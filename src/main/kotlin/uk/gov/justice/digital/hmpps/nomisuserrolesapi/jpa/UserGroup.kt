package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

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
  @Convert(converter = YesNoConverter::class)
  val active: Boolean,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE")
  @BatchSize(size = 1000)
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

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE")
  val userGroups: List<GroupCaseload> = listOf(),

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserGroup

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun toString(): String = this::class.simpleName + "(code = $id )"
}
