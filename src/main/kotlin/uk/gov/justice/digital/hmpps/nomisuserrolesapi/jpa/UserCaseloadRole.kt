package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.Objects

@Entity
@Table(name = "USER_CASELOAD_ROLES")
data class UserCaseloadRole(
  @EmbeddedId
  val id: UserCaseloadRoleIdentity,

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ROLE_ID", updatable = false, insertable = false)
  val role: Role,

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumns(
    value = [
      JoinColumn(name = "CASELOAD_ID", referencedColumnName = "CASELOAD_ID", insertable = false, updatable = false),
      JoinColumn(name = "USERNAME", referencedColumnName = "USERNAME", insertable = false, updatable = false),
    ],
  )
  val userCaseload: UserCaseload,

) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserCaseloadRole

    return id == other.id
  }

  override fun hashCode(): Int = Objects.hash(id)

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(EmbeddedId = $id )"
  }
}
