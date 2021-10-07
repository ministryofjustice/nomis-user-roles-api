package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "OMS_ROLES")
data class Role(
  @Id
  @SequenceGenerator(name = "ROLE_ID", sequenceName = "ROLE_ID", allocationSize = 1)
  @GeneratedValue(generator = "ROLE_ID")
  @Column(name = "ROLE_ID", nullable = false)
  val id: Long,

  @Column(name = "ROLE_CODE", nullable = false, unique = true)
  val code: String,

  @Column(name = "ROLE_NAME")
  val name: String? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Role

    return id == other.id
  }

  override fun hashCode(): Int = code.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(code = $code )"
  }
}
