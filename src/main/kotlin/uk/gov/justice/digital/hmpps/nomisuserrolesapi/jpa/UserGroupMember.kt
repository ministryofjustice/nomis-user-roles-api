package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import java.io.Serializable
import java.time.LocalDate

@Suppress("DataClassEqualsAndHashCodeInspection")
@Embeddable
data class UserGroupMemberPk(
  @Column(name = "LOCAL_AUTHORITY_CODE", nullable = false)
  var userGroupCode: String,
  @Column(name = "USERNAME", nullable = false)
  var username: String,
) : Serializable

@Entity
@Table(name = "LAA_GENERAL_USERS")
data class UserGroupMember(

  @EmbeddedId
  val id: UserGroupMemberPk,

  @Column(name = "ACTIVE_FLAG")
  @Convert(converter = YesNoConverter::class)
  val active: Boolean = true,

  @Column(name = "START_DATE")
  val startDate: LocalDate = LocalDate.now(),

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @ManyToOne(optional = false, fetch = LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE", nullable = false, updatable = false, insertable = false)
  val userGroup: UserGroup,

  @ManyToOne
  @JoinColumn(name = "USERNAME", nullable = false, updatable = false, insertable = false)
  val user: UserPersonDetail,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserGroupMember

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun toString(): String {
    return this::class.simpleName + id.toString()
  }
}
