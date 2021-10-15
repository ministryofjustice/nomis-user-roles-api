package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Where
import java.time.LocalDate.now
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
  val dpsRoles: MutableList<UserCaseloadRole> = mutableListOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "user", orphanRemoval = true)
  var caseloads: List<UserCaseload> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  var activeAndInactiveMemberOfUserGroups: List<UserGroupMember> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val memberOfUserGroups: List<UserGroupMember> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  var activeAndInactiveAdministratorOfUserGroups: List<UserGroupAdministrator> = listOf(),

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

  fun addCaseload(caseload: Caseload) {
    val userCaseload = UserCaseload(
      id = UserCaseloadPk(caseloadId = caseload.id, username = this.username),
      caseload = caseload, user = this, startDate = now(),
      roles = listOf(),
    )
    caseloads = caseloads + userCaseload
  }

  fun addUserGroup(userGroup: UserGroup) {
    val member = UserGroupMember(
      id = UserGroupMemberPk(userGroupCode = userGroup.id, username = this.username),
      user = this, userGroup = userGroup
    )
    activeAndInactiveMemberOfUserGroups = activeAndInactiveMemberOfUserGroups + member
  }

  fun addAdminUserGroup(userGroup: UserGroup) {
    val adminMember = UserGroupAdministrator(
      id = UserGroupAdministratorPk(userGroupCode = userGroup.id, username = this.username),
      user = this, userGroup = userGroup
    )
    activeAndInactiveAdministratorOfUserGroups = activeAndInactiveAdministratorOfUserGroups + adminMember
  }
}
