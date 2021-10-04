package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "LOCAL_ADMIN_AUTHORITIES")
data class UserGroup(
  @Id
  @Column(name = "LOCAL_AUTHORITY_CODE", nullable = false)
  val id: String,

  @Column(name = "DESCRIPTION", nullable = false)
  val description: String,

  @Column(name = "LOCAL_AUTHORITY_CATEGORY")
  val category: String,

  @Column(name = "ACTIVE_FLAG")
  @Type(type = "yes_no")
  val active: Boolean,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE")
  val activeAndInactiveMembers: List<UserGroupMember> = listOf(),

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE")
  val activeAndInactiveAdministrators: List<UserGroupAdministrator> = listOf(),

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE")
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val members: List<UserGroupMember> = listOf(),

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE")
  @Where(clause = "ACTIVE_FLAG = 'Y'")
  val administrators: List<UserGroupAdministrator> = listOf(),

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserGroup

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun toString(): String {
    return this::class.simpleName + "(code = $id )"
  }
}
