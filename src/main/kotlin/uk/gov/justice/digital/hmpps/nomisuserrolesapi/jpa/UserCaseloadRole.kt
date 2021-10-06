package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.Objects
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

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
      JoinColumn(name = "USERNAME", referencedColumnName = "USERNAME", insertable = false, updatable = false)
    ]
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
