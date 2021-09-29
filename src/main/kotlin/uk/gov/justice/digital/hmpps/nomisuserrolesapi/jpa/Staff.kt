package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "STAFF_MEMBERS")
data class Staff(
  @Id
  @SequenceGenerator(name = "STAFF_ID", sequenceName = "STAFF_ID", allocationSize = 1)
  @GeneratedValue(generator = "STAFF_ID")
  @Column(name = "STAFF_ID", nullable = false)
  var staffId: Long = 0,

  @Column(name = "FIRST_NAME", nullable = false)
  val firstName: String,

  @Column(name = "LAST_NAME", nullable = false)
  val lastName: String,

  @Column(name = "STATUS")
  val status: String,
) {

  val isActive: Boolean
    get() = STAFF_STATUS_ACTIVE == status

  companion object {
    private const val STAFF_STATUS_ACTIVE = "ACTIVE"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Staff

    return staffId == other.staffId
  }

  override fun hashCode(): Int = 0

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(staffId = $staffId )"
  }
}
