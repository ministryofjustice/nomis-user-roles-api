package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "STAFF_MEMBERS")
data class Staff(
  @Id
  @Column(name = "STAFF_ID", nullable = false)
  val staffId: Long,

  @Column(name = "FIRST_NAME", nullable = false)
  private val firstName: String,

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
}
