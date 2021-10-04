import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroup
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministrator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministratorPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class UserSpecification(private val filter: UserFilter) : Specification<UserPersonDetail> {
  override fun toPredicate(
    root: Root<UserPersonDetail>,
    query: CriteriaQuery<*>,
    criteriaBuilder: CriteriaBuilder
  ): Predicate? {
    val predicates = mutableListOf<Predicate>()

    fun administeredBy(localAdministratorUsername: String): Predicate {
      val memberOfUserGroupsJoin =
        root.join<UserPersonDetail, UserGroupMember>(UserPersonDetail::memberOfUserGroups.name)
      val userGroupsJoin = memberOfUserGroupsJoin.join<UserGroupMember, UserGroup>(UserGroupMember::userGroup.name)
      val administratorsJoin = userGroupsJoin.join<UserGroup, UserGroupAdministrator>(UserGroup::administrators.name)
      return criteriaBuilder.equal(
        administratorsJoin.get<UserGroupAdministratorPk>(UserGroupAdministrator::id.name)
          .get<String>(UserGroupAdministratorPk::username.name),
        localAdministratorUsername
      )
    }

    filter.localAdministratorUsername?.run {
      predicates.add(administeredBy(this))
    }

    return criteriaBuilder.and(*predicates.toTypedArray())
  }
}

data class UserFilter(val localAdministratorUsername: String? = null)
