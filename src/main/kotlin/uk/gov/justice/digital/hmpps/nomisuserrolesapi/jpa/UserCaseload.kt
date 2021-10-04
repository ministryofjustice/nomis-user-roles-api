@file:Suppress("DataClassEqualsAndHashCodeInspection")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import java.io.Serializable
import java.time.LocalDate
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Embeddable
data class UserCaseloadPk(
  @Column(name = "CASELOAD_ID", nullable = false)
  val caseloadId: String,

  @Column(name = "USERNAME", nullable = false)
  val username: String,
) : Serializable

@Entity
@Table(name = "USER_ACCESSIBLE_CASELOADS")
data class UserCaseload(
  @EmbeddedId
  val id: UserCaseloadPk,

  @ManyToOne
  @JoinColumn(name = "CASELOAD_ID", updatable = false, insertable = false)
  val caseload: Caseload,

  @ManyToOne
  @JoinColumn(name = "USERNAME", updatable = false, insertable = false)
  val user: UserPersonDetail,

  @JoinColumn(name = "START_DATE")
  val startDate: LocalDate,

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserCaseload

    return id == other.id
  }

  override fun hashCode(): Int = Objects.hash(id)

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(EmbeddedId = $id )"
  }
}
