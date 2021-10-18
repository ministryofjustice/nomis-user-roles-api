package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.CaseloadNotFoundException
import java.time.LocalDate.now
import java.util.function.Supplier
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Suppress("DataClassEqualsAndHashCodeInspection")
@Entity
@Table(name = "STAFF_USER_ACCOUNTS")
data class UserPersonDetail(
  @Id
  @Column(name = "USERNAME", nullable = false)
  val username: String,

  @ManyToOne(cascade = [CascadeType.ALL])
  @JoinColumn(name = "STAFF_ID")
  val staff: Staff,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "USERNAME", updatable = false, insertable = false, nullable = false)
  @Where(clause = "CASELOAD_ID = 'NWEB'")
  val dpsRoles: List<UserCaseloadRole> = listOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "user", orphanRemoval = true)
  val caseloads: MutableList<UserCaseload> = mutableListOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val activeAndInactiveMemberOfUserGroups: MutableList<UserGroupMember> = mutableListOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val memberOfUserGroups: List<UserGroupMember> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val activeAndInactiveAdministratorOfUserGroups: MutableList<UserGroupAdministrator> = mutableListOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val administratorOfUserGroups: List<UserGroupAdministrator> = listOf(),

  @Column(name = "STAFF_USER_TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  val type: UsageType,

  @JoinColumn(name = "WORKING_CASELOAD_ID", nullable = true)
  @ManyToOne
  var activeCaseLoad: Caseload? = null,

  @Column(name = "ID_SOURCE")
  var idSource: String = "USER",
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserPersonDetail

    return username == other.username
  }

  override fun hashCode(): Int = username.hashCode()

  private fun findCaseloadById(caseloadId: String): Caseload? {
    return caseloads.firstOrNull { caseload -> caseloadId == caseload.id.caseloadId }?.caseload
  }

  fun setDefaultCaseload(caseloadId: String) {
    activeCaseLoad = findCaseloadById(caseloadId) ?: throw CaseloadNotFoundException("Default caseload cannot be set as user does not have $caseloadId.")
  }

  fun addCaseload(caseload: Caseload) {
    findCaseloadById(caseloadId = caseload.id)?.let { throw CaseloadAlreadyExistsException("Caseload ${it.id} already added to this user") }

    val userCaseload = UserCaseload(
      id = UserCaseloadPk(caseloadId = caseload.id, username = this.username),
      caseload = caseload, user = this, startDate = now(),
      roles = listOf(),
    )
    caseloads.add(userCaseload)

    if (type == UsageType.GENERAL) {
      caseload.userGroups.forEach { addUserGroup(it.userGroup) }
    }
  }

  fun addToAdminUserGroup(caseload: Caseload) {
    caseload.userGroups.forEach { addAdminUserGroup(it.userGroup) }
  }

  fun removeCaseload(caseloadId: String) {
    val userCaseload = caseloads.firstOrNull { caseload -> caseloadId == caseload.id.caseloadId } ?: throw CaseloadNotFoundException("Caseload cannot be removed as user does not have $caseloadId.")
    caseloads.remove(userCaseload)
  }

  fun addUserGroup(userGroup: UserGroup) {
    val member = UserGroupMember(
      id = UserGroupMemberPk(userGroupCode = userGroup.id, username = this.username),
      user = this, userGroup = userGroup
    )
    activeAndInactiveMemberOfUserGroups.add(member)
  }

  fun addAdminUserGroup(userGroup: UserGroup) {
    val adminMember = UserGroupAdministrator(
      id = UserGroupAdministratorPk(userGroupCode = userGroup.id, username = this.username),
      user = this, userGroup = userGroup
    )
    activeAndInactiveAdministratorOfUserGroups.add(adminMember)
  }
}

class CaseloadAlreadyExistsException(message: String?) :
  RuntimeException(message),
  Supplier<CaseloadAlreadyExistsException> {
  override fun get(): CaseloadAlreadyExistsException {
    return CaseloadAlreadyExistsException(message)
  }
}
