package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummaryWithEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.AbbreviationsProcessor
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.CaseloadNotFoundException
import java.time.LocalDate.now
import java.util.function.Supplier
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedSubgraph
import javax.persistence.OneToMany
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.SecondaryTable
import javax.persistence.Table

@Suppress("DataClassEqualsAndHashCodeInspection")
@Entity
@Table(name = "STAFF_USER_ACCOUNTS")
@SecondaryTable(name = "DBA_USERS", pkJoinColumns = [PrimaryKeyJoinColumn(name = "USERNAME")])
@BatchSize(size = 100)

@NamedEntityGraph(
  name = "user-person-detail-download-graph",
  attributeNodes = [
    NamedAttributeNode("username"), NamedAttributeNode(value = "administratorOfUserGroups"),
    NamedAttributeNode(value = "staff", subgraph = "staff-subgraph")
  ],
  subgraphs = [
    NamedSubgraph(
      name = "staff-subgraph",
      attributeNodes = [
        NamedAttributeNode(value = "staffId"), NamedAttributeNode(value = "firstName"), NamedAttributeNode(
          value = "lastName"
        ), NamedAttributeNode(value = "status"), NamedAttributeNode(
          value = "emails",
          subgraph = "emails-subgraph"
        )
      ]
    ),
    NamedSubgraph(
      name = "emails-subgraph",
      attributeNodes = [
        NamedAttributeNode(value = "userType"),
        NamedAttributeNode(value = "type"), NamedAttributeNode(value = "email")
      ]
    ),
    NamedSubgraph(
      name = "usergroup-admin-subgraph",
      attributeNodes = [
        NamedAttributeNode(value = "id"), NamedAttributeNode(value = "active")
      ]
    )
  ]
)
data class UserPersonDetail(
  @Id
  @Column(name = "USERNAME", nullable = false)
  val username: String,

  @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "STAFF_ID", nullable = false)
  val staff: Staff,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "USERNAME", updatable = false, insertable = false, nullable = false)
  @Where(clause = "CASELOAD_ID = '$DPS_CASELOAD'")
  @BatchSize(size = 100)
  val dpsRoles: List<UserCaseloadRole> = listOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "user", orphanRemoval = true)
  @BatchSize(size = 100)
  val caseloads: MutableList<UserCaseload> = mutableListOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @BatchSize(size = 100)
  val activeAndInactiveMemberOfUserGroups: MutableList<UserGroupMember> = mutableListOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  @BatchSize(size = 100)
  val memberOfUserGroups: List<UserGroupMember> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @BatchSize(size = 100)
  val activeAndInactiveAdministratorOfUserGroups: MutableList<UserGroupAdministrator> = mutableListOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @BatchSize(size = 100)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val administratorOfUserGroups: List<UserGroupAdministrator> = listOf(),

  @Column(name = "STAFF_USER_TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  val type: UsageType,

  @JoinColumn(name = "WORKING_CASELOAD_ID", nullable = true)
  @ManyToOne(fetch = FetchType.LAZY)
  @BatchSize(size = 100)
  var activeCaseLoad: Caseload? = null,

  @Column(name = "ID_SOURCE")
  var idSource: String = "USER",

  @Embedded
  val accountDetail: AccountDetail? = AccountDetail(),
) {

  fun isActive() = accountDetail?.isActive() ?: false

  fun isEnabled() = accountDetail?.isEnabled() ?: false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserPersonDetail

    return username == other.username
  }

  override fun hashCode(): Int = username.hashCode()

  private fun findUserCaseloadById(caseloadId: String): UserCaseload? {
    return caseloads.firstOrNull { caseloadId == it.id.caseloadId }
  }

  fun findCaseloadById(caseloadId: String): Caseload? {
    return findUserCaseloadById(caseloadId = caseloadId)?.caseload
  }

  fun setDefaultCaseload(caseloadId: String) {
    activeCaseLoad = findCaseloadById(caseloadId) ?: throw CaseloadNotFoundException("Default caseload cannot be set as user does not have $caseloadId.")
  }

  fun addCaseload(caseload: Caseload) {
    findCaseloadById(caseloadId = caseload.id)?.run { throw CaseloadAlreadyExistsException("Caseload ${caseload.id} already added to this user") }

    val userCaseload = UserCaseload(
      id = UserCaseloadPk(caseloadId = caseload.id, username = this.username),
      caseload = caseload, user = this, startDate = now(),
      roles = mutableListOf(),
    )
    caseloads.add(userCaseload)

    if (isUserGroupCaseload(caseload)) {
      caseload.userGroups.forEach { addUserGroup(it.userGroup) }
    }
  }

  fun addToAdminUserGroup(caseload: Caseload) {
    caseload.userGroups.forEach { addAdminUserGroup(it.userGroup) }
  }

  fun removeCaseload(caseloadId: String) {
    val userCaseload = caseloads.firstOrNull { caseload -> caseloadId == caseload.id.caseloadId } ?: throw CaseloadNotFoundException("Caseload cannot be removed as user does not have $caseloadId.")

    if (isUserGroupCaseload(userCaseload.caseload)) {
      val userGroupMembersAssociatedWithCaseload = activeAndInactiveMemberOfUserGroups.filter { userGroupMember -> isCaseloadForUserGroup(userCaseload.caseload, userGroupMember) }
      activeAndInactiveMemberOfUserGroups.removeAll(userGroupMembersAssociatedWithCaseload)
    }

    caseloads.remove(userCaseload)
  }

  private fun isUserGroupCaseload(caseload: Caseload) = (type == UsageType.GENERAL) && !caseload.isDpsCaseload()

  private fun isCaseloadForUserGroup(caseload: Caseload, userGroupMember: UserGroupMember): Boolean {
    return caseload.userGroups.firstOrNull { groupCaseload -> groupCaseload.userGroup === userGroupMember.userGroup && userGroupMember.user === this } !== null
  }

  private fun addUserGroup(userGroup: UserGroup) {
    activeAndInactiveMemberOfUserGroups.firstOrNull { it.id.userGroupCode == userGroup.id && it.id.username == this.username } ?: run {
      val member = UserGroupMember(
        id = UserGroupMemberPk(userGroupCode = userGroup.id, username = this.username),
        user = this, userGroup = userGroup
      )
      activeAndInactiveMemberOfUserGroups.add(member)
    }
  }

  private fun addAdminUserGroup(userGroup: UserGroup) {
    val adminMember = UserGroupAdministrator(
      id = UserGroupAdministratorPk(userGroupCode = userGroup.id, username = this.username),
      user = this, userGroup = userGroup
    )
    activeAndInactiveAdministratorOfUserGroups.add(adminMember)
  }

  fun addRole(role: Role, caseloadId: String = DPS_CASELOAD) {
    val userCaseload = findUserCaseloadById(caseloadId = caseloadId)
      ?: throw CaseloadNotFoundException("User does not have access to caseload $caseloadId")
    userCaseload.addRole(role)
  }

  fun removeRole(roleCode: String, caseloadId: String = DPS_CASELOAD) {
    val userCaseload = findUserCaseloadById(caseloadId = caseloadId)
      ?: throw CaseloadNotFoundException("User does not have access to caseload $caseloadId")

    userCaseload.removeRole(roleCode)
  }
}

class CaseloadAlreadyExistsException(message: String?) :
  RuntimeException(message),
  Supplier<CaseloadAlreadyExistsException> {
  override fun get(): CaseloadAlreadyExistsException {
    return CaseloadAlreadyExistsException(message)
  }
}

fun UserPersonDetail.toUserSummaryWithEmail() = UserSummaryWithEmail(
  username = username,
  staffId = staff.staffId,
  firstName = staff.firstName.capitalizeFully(),
  lastName = staff.lastName.capitalizeFully(),
  active = isActive(),
  status = accountDetail?.status,
  locked = accountDetail?.isLocked() ?: false,
  expired = accountDetail?.isExpired() ?: false,
  activeCaseload = activeCaseLoad?.let { caseload ->
    PrisonCaseload(
      id = caseload.id,
      name = caseload.name.capitalizeLeavingAbbreviations()
    )
  },
  dpsRoleCount = this.dpsRoles.size,
  email = staff.primaryEmail()?.email,
)

fun UserPersonDetail.toDownloadUserSummaryWithEmail() = UserSummaryWithEmail(
  username = username,
  staffId = staff.staffId,
  firstName = staff.firstName.capitalizeFully(),
  lastName = staff.lastName.capitalizeFully(),
  active = isActive(),
  status = accountDetail?.status,
  locked = accountDetail?.isLocked() ?: false,
  expired = accountDetail?.isExpired() ?: false,
  activeCaseload = this.activeCaseLoad?.let { caseload ->
    PrisonCaseload(
      id = caseload.id,
      name = caseload.name.capitalizeLeavingAbbreviations()
    )
  },
  dpsRoleCount = 0,
  email = staff.primaryEmail()?.email,
)

fun UserPersonDetail.toUserSummary(): UserSummary = UserSummary(
  username = this.username,
  staffId = this.staff.staffId,
  firstName = this.staff.firstName.capitalizeFully(),
  lastName = this.staff.lastName.capitalizeFully(),
  active = isActive(),
  activeCaseload = this.activeCaseLoad?.let { caseload ->
    PrisonCaseload(
      id = caseload.id,
      name = caseload.name.capitalizeLeavingAbbreviations()
    )
  },
  dpsRoleCount = this.dpsRoles.size,
)

private fun String.capitalizeLeavingAbbreviations() = AbbreviationsProcessor.capitalizeLeavingAbbreviations(this)
