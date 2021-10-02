package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Where
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
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
  @JoinColumn(name = "USERNAME")
  val roles: List<UserCaseloadRole> = listOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "user")
  val caseloads: List<UserCaseload> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val activeAndInactiveMemberOfUserGroups: List<UserGroupMember> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val memberOfUserGroups: List<UserGroupMember> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val activeAndInactiveAdministratorOfUserGroups: List<UserGroupAdministrator> = listOf(),

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val administratorOfUserGroups: List<UserGroupAdministrator> = listOf(),

  @Column(name = "STAFF_USER_TYPE", nullable = false)
  val type: String,

  @JoinColumn(name = "WORKING_CASELOAD_ID", nullable = true, insertable = false, updatable = false)
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
}
