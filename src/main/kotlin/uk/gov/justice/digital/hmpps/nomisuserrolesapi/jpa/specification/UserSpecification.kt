import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroup
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministrator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministratorPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Join
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import kotlin.reflect.KProperty1

class UserSpecification(private val filter: UserFilter) : Specification<UserPersonDetail> {
  override fun toPredicate(
    root: Root<UserPersonDetail>,
    query: CriteriaQuery<*>,
    criteriaBuilder: CriteriaBuilder
  ): Predicate? {
    val predicates = mutableListOf<Predicate>()

    // these allow a fluent version based on the types being passed and rely on closure for criteria and root
    fun <PROP> Path<*>.get(prop: KProperty1<*, PROP>): Path<PROP> = this.get(prop.name)
    fun <PROP> Root<*>.get(prop: KProperty1<*, PROP>): Path<PROP> = this.get(prop.name)
    fun <PROP> get(prop: KProperty1<*, PROP>): Path<PROP> = root.get(prop)
    fun <FROM, TO> join(prop: KProperty1<FROM, List<TO>>): Join<FROM, TO> = root.join(prop.name)
    fun <FROM, TO, NEXT> Join<FROM, TO>.join(prop: KProperty1<*, NEXT>): Join<TO, NEXT> = this.join(prop.name)
    fun or(vararg predicates: Predicate) = criteriaBuilder.or(*predicates)
    fun and(vararg predicates: Predicate) = criteriaBuilder.and(*predicates)
    fun like(x: Expression<String>, pattern: String) = criteriaBuilder.like(x, pattern)
    fun equal(x: Expression<String>, pattern: String) = criteriaBuilder.equal(x, pattern)
    fun inList(x: Expression<String>, values: List<String>) = x.`in`(values)

    fun administeredBy(localAdministratorUsername: String): Predicate {
      return equal(
        join(UserPersonDetail::memberOfUserGroups)
          .join(UserGroupMember::userGroup)
          .join(UserGroup::administrators)
          .get(UserGroupAdministrator::id)
          .get(UserGroupAdministratorPk::username),
        localAdministratorUsername
      )
    }

    fun nameMatch(name: String): Predicate =
      if (name.isFullName()) {
        or(
          and(
            like(
              get(UserPersonDetail::staff).get(Staff::firstName),
              name.firstWord().uppercaseLike()
            ),
            like(
              get(UserPersonDetail::staff).get(Staff::lastName),
              name.secondWord().uppercaseLike()
            ),
          ),
          and(
            like(
              get(UserPersonDetail::staff).get(Staff::firstName),
              name.secondWord().uppercaseLike()
            ),
            like(
              get(UserPersonDetail::staff).get(Staff::lastName),
              name.firstWord().uppercaseLike()
            ),
          ),
        )
      } else {
        or(
          like(
            get(UserPersonDetail::staff).get(Staff::firstName),
            name.uppercaseLike()
          ),
          like(
            get(UserPersonDetail::staff).get(Staff::lastName),
            name.uppercaseLike()
          ),
          like(
            get(UserPersonDetail::username),
            name.uppercaseLike()
          ),
        )
      }

    fun statusMatch(status: UserStatus): Predicate? =
      status.databaseStatus()?.let {
        inList(get(UserPersonDetail::accountDetail).get(AccountDetail::accountStatus), it)
      }

    fun activeCaseload(caseloadId: String): Predicate =
      equal(get(UserPersonDetail::activeCaseLoad).get(Caseload::id), caseloadId)

    fun caseload(caseloadId: String): Predicate =
      equal(join(UserPersonDetail::caseloads).get(Caseload::id).get(UserCaseloadPk::caseloadId), caseloadId)

    fun nomisRoles(caseloadId: String, roleCode: String): Predicate {
      val userCaseload = join(UserPersonDetail::caseloads)
      return and(
        equal(userCaseload.get(Caseload::id).get(UserCaseloadPk::caseloadId), caseloadId),
        equal(userCaseload.join(UserCaseload::roles).get(UserCaseloadRole::role).get(Role::code), roleCode)
      )
    }

    fun nomisRoles(roleCode: String): Predicate =
      and(
        equal(join(UserPersonDetail::caseloads).join(UserCaseload::roles).get(UserCaseloadRole::role).get(Role::code), roleCode)
      )

    fun roles(roleCodes: List<String>): Predicate =
      and(
        * roleCodes.map {
          equal(join(UserPersonDetail::dpsRoles).get(UserCaseloadRole::role).get(Role::code), it)
        }.toTypedArray()
      )

    filter.localAdministratorUsername?.run {
      predicates.add(administeredBy(this))
    }

    filter.name?.run {
      predicates.add(nameMatch(this))
    }

    filter.status?.run {
      statusMatch(this)?.run { predicates.add(this) }
    }

    filter.activeCaseloadId?.run {
      predicates.add(activeCaseload(this))
    }

    filter.caseloadId?.run {
      filter.nomisRoleCode?.run {
        predicates.add(nomisRoles(filter.caseloadId, this))
      }
        ?: predicates.add(caseload(this))
    }

    filter.nomisRoleCode?.run {
      filter.caseloadId
        ?: predicates.add(nomisRoles(this))
    }

    filter.roleCodes.takeIf { it.isEmpty().not() }?.run {
      predicates.add(roles(this))
    }

    return criteriaBuilder.and(*predicates.toTypedArray())
  }
}

private fun UserStatus.databaseStatus(): List<String>? = when (this) {
  UserStatus.ALL -> null
  UserStatus.ACTIVE -> AccountStatus.activeStatuses().map { it.desc }
  UserStatus.INACTIVE -> AccountStatus.inActiveStatuses().map { it.desc }
}

private fun String.spiltWords(): List<String> = this.split(",", " ")
private fun String.isFullName() = this.spiltWords().size > 1
private fun String.asFullName(): Pair<String, String> = this.spiltWords().let { it[0] to it[1] }
private fun String.firstWord() = asFullName().first
private fun String.secondWord() = asFullName().second

private fun String.uppercaseLike() = "${this.uppercase()}%"
