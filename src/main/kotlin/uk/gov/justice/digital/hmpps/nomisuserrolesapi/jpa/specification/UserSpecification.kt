import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
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
    fun or(vararg predicates: Predicate) = criteriaBuilder.or(*predicates)
    fun and(vararg predicates: Predicate) = criteriaBuilder.and(*predicates)
    fun like(x: Expression<String>, pattern: String) = criteriaBuilder.like(x, pattern)
    fun equal(x: Expression<String>, pattern: String) = criteriaBuilder.equal(x, pattern)
    fun staff() = root.get<Staff>(UserPersonDetail::staff.name)
    fun username() = root.get<String>(UserPersonDetail::username.name)
    fun joinToMemberOfUserGroups() =
      root.join<UserPersonDetail, UserGroupMember>(UserPersonDetail::memberOfUserGroups.name)

    fun Join<UserPersonDetail, UserGroupMember>.joinToUserGroup() =
      this.join<UserGroupMember, UserGroup>(UserGroupMember::userGroup.name)

    fun Join<UserGroupMember, UserGroup>.joinToAdministrators() =
      this.join<UserGroup, UserGroupAdministrator>(UserGroup::administrators.name)

    fun <PROP> Path<*>.get(prop: KProperty1<*, PROP>): Path<PROP> = this.get(prop.name)

    fun administeredBy(localAdministratorUsername: String): Predicate {
      return equal(
        joinToMemberOfUserGroups()
          .joinToUserGroup()
          .joinToAdministrators()
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
              staff().get(Staff::firstName),
              name.firstWord().uppercaseLike()
            ),
            like(
              staff().get(Staff::lastName),
              name.secondWord().uppercaseLike()
            ),
          ),
          and(
            like(
              staff().get(Staff::firstName),
              name.secondWord().uppercaseLike()
            ),
            like(
              staff().get(Staff::lastName),
              name.firstWord().uppercaseLike()
            ),
          ),
        )
      } else {
        or(
          like(
            staff().get(Staff::firstName),
            name.uppercaseLike()
          ),
          like(
            staff().get(Staff::lastName),
            name.uppercaseLike()
          ),
          like(
            username(),
            name.uppercaseLike()
          ),
        )
      }

    filter.localAdministratorUsername?.run {
      predicates.add(administeredBy(this))
    }

    filter.name?.run {
      predicates.add(nameMatch(this))
    }

    return criteriaBuilder.and(*predicates.toTypedArray())
  }
}

private fun String.spiltWords(): List<String> = this.split(",", " ")
private fun String.isFullName() = this.spiltWords().size > 1
private fun String.asFullName(): Pair<String, String> = this.spiltWords().let { it[0] to it[1] }
private fun String.firstWord() = asFullName().first
private fun String.secondWord() = asFullName().second

private fun String.uppercaseLike() = "${this.uppercase()}%"
