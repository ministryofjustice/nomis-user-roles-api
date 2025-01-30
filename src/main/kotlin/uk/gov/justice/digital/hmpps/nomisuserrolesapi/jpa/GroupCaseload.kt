package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import java.io.Serializable
import java.time.LocalDate

@Suppress("DataClassEqualsAndHashCodeInspection")
@Embeddable
data class GroupCaseloadPk(
  @Column(name = "CASELOAD_ID", nullable = false)
  var caseload: String,
  @Column(name = "LOCAL_AUTHORITY_CODE", nullable = false)
  var userGroupCode: String,
) : Serializable

@Entity
@Table(name = "LAA_CASELOADS")
data class GroupCaseload(
  @EmbeddedId
  val id: GroupCaseloadPk,

  @Column(name = "ACTIVE_FLAG")
  @Convert(converter = YesNoConverter::class)
  val active: Boolean = true,

  @Column(name = "EXPIRY_DATE")
  val expiryDate: LocalDate? = null,

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_AUTHORITY_CODE", nullable = false, updatable = false, insertable = false)
  val userGroup: UserGroup,

  @ManyToOne
  @JoinColumn(name = "CASELOAD_ID", nullable = false, updatable = false, insertable = false)
  val caseload: Caseload,

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as GroupCaseload

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()

  override fun toString(): String = this::class.simpleName + "(code = $id )"
}
