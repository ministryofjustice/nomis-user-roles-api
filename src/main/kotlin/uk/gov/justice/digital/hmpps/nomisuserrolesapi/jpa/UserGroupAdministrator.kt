@file:Suppress("DataClassEqualsAndHashCodeInspection")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Embeddable
data class UserGroupAdministratorPk(
  @Column(name = "LOCAL_AUTHORITY_CODE", nullable = false)
  var userGroupCode: String,
  @Column(name = "USERNAME", nullable = false)
  var username: String,
) : Serializable

@Entity
@Table(name = "LAA_ADMINISTRATORS")
data class UserGroupAdministrator(

  @EmbeddedId
  val id: UserGroupAdministratorPk,

  @Column(name = "ACTIVE_FLAG")
  @Type(type = "yes_no")
  val active: Boolean,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE", nullable = false, updatable = false, insertable = false)
  val userGroup: UserGroup,

  @ManyToOne
  @JoinColumn(name = "USERNAME", nullable = false, updatable = false, insertable = false)
  val user: UserPersonDetail,

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserGroupAdministrator

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun toString(): String {
    return this::class.simpleName + id.toString()
  }
}
