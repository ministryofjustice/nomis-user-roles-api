package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType.LAZY
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Suppress("DataClassEqualsAndHashCodeInspection")
@Embeddable
data class RoleCaseloadPk(
  @Column(name = "ROLE_ID", nullable = false)
  var roleId: Long,
  @Column(name = "LOCAL_AUTHORITY_CODE", nullable = false)
  var caseload: String,
) : Serializable

@Entity
@Table(name = "LAA_GRANTED_ROLES")
data class RoleCaseload(

  @EmbeddedId
  val id: RoleCaseloadPk,

  @Column(name = "ACTIVE_FLAG")
  @Type(type = "yes_no")
  val active: Boolean,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @ManyToOne(optional = false, fetch = LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE", nullable = false, updatable = false, insertable = false)
  val caseload: Caseload,

  @ManyToOne
  @JoinColumn(name = "ROLE_ID", nullable = false, updatable = false, insertable = false)
  val role: Role,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as RoleCaseload

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun toString(): String {
    return this::class.simpleName + id.toString()
  }
}
