package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.springframework.data.repository.CrudRepository
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "STAFF_LOCATION_ROLES")
@IdClass(StaffLocationRoleId::class)
class StaffLocationRole(
  @Id
  @Column(name = "CAL_AGY_LOC_ID")
  val agencyId: String,

  @Id
  @Column(name = "SAC_STAFF_ID")
  val staffId: Long,

  @Id
  @Column(name = "FROM_DATE")
  val fromDate: LocalDate,

  @Id
  @Column(name = "POSITION")
  val position: String,

  @Id
  @Column(name = "ROLE")
  val role: String,
) {
  @Column(name = "TO_DATE")
  var toDate: LocalDate? = null
    private set

  @Column(name = "SCHEDULE_TYPE")
  var scheduleType: String = ""
    private set

  @Column(name = "HOURS_PER_WEEK")
  var hoursPerWeek: Int = 0
    private set

  fun modify(toDate: LocalDate?, scheduleType: ReferenceData, hoursPerWeek: Int): Boolean {
    val changeDetected =
      this.toDate != toDate || this.scheduleType != scheduleType.code || this.hoursPerWeek != hoursPerWeek
    this.toDate = toDate
    this.scheduleType = scheduleType.code
    this.hoursPerWeek = hoursPerWeek
    return changeDetected
  }

  fun expire() = apply { toDate = LocalDate.now() }
}

data class StaffLocationRoleId(
  val agencyId: String? = null,
  val staffId: Long? = null,
  val role: String? = null,
  val position: String? = null,
  val fromDate: LocalDate? = null,
) : Serializable

interface StaffLocationRoleRepository : CrudRepository<StaffLocationRole, StaffLocationRoleId> {
  fun findAllByAgencyIdAndStaffIdAndRole(agencyId: String, staffId: Long, role: String): List<StaffLocationRole>
}
