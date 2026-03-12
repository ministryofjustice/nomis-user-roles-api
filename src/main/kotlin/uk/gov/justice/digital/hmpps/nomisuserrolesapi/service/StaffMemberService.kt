package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.validation.ValidationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.JobClassificationRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffJobClassification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceData
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataDomain.SCHEDULE_TYP
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataDomain.STAFF_POS
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataDomain.STAFF_ROLE
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRoleId
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.findByKeyIn
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.of
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import java.time.format.DateTimeFormatter.ISO_DATE

@Transactional
@Service
class StaffMemberService(
  private val staffRepository: StaffRepository,
  private val referenceDataRepository: ReferenceDataRepository,
  private val staffLocationRoleRepository: StaffLocationRoleRepository,
  private val telemetryClient: TelemetryClient,
) {
  fun setJobClassification(
    agencyId: String,
    staffId: Long,
    staffRole: String,
    jobClassification: JobClassificationRequest,
  ): StaffJobClassification {
    val staff = staffValidation(agencyId, staffId)
    val (staffRole, position, scheduleType) = jobClassification.referenceData(staffRole)
    val existing = staffLocationRoleRepository.findByIdOrNull(
      StaffLocationRoleId(
        agencyId,
        staff.staffId,
        staffRole.code,
        position.code,
        jobClassification.fromDate,
      ),
    )?.apply {
      if (modify(jobClassification.toDate, scheduleType, jobClassification.hoursPerWeek)) {
        telemetryClient.trackEvent(STAFF_LOCATION_ROLE_UPDATED, telemetryProperties(), null)
      }
    }

    return if (existing == null) {
      staffLocationRoleRepository.findAllByAgencyIdAndStaffIdAndRole(agencyId, staffId, staffRole.code)
        .forEach {
          if (it.toDate == null) it.expire()
          telemetryClient.trackEvent(STAFF_LOCATION_ROLE_UPDATED, it.telemetryProperties(), null)
        }
      staffLocationRoleRepository.save(
        StaffLocationRole(
          agencyId,
          staffId,
          jobClassification.fromDate,
          position.code,
          staffRole.code,
        ).apply {
          modify(jobClassification.toDate, scheduleType, jobClassification.hoursPerWeek)
          telemetryClient.trackEvent(STAFF_LOCATION_ROLE_CREATED, telemetryProperties(), null)
        },
      ).asStaffJobClassification()
    } else {
      existing.asStaffJobClassification()
    }
  }

  private fun JobClassificationRequest.referenceData(staffRole: String): ReferenceCodes {
    val referenceData = referenceDataRepository.findByKeyIn(
      STAFF_ROLE of staffRole,
      STAFF_POS of position,
      SCHEDULE_TYP of scheduleType,
    ).associateBy { it.key.domain }
    val role = referenceData[STAFF_ROLE] ?: throw ValidationException("Staff role not found")
    val position = referenceData[STAFF_POS] ?: throw ValidationException("Staff position not found")
    val scheduleType = referenceData[SCHEDULE_TYP] ?: throw ValidationException("Schedule type not found")
    return ReferenceCodes(role, position, scheduleType)
  }

  private fun staffValidation(agencyId: String, staffId: Long): Staff {
    val staff = staffRepository.findByIdOrNull(staffId)
      ?.takeIf { it.isActive }
      ?: throw StaffNotFoundException(staffId)

    val staffInAgency = staff.users.flatMap { it.caseloads }.any { it.id.caseloadId == agencyId }
    if (!staffInAgency) throw ValidationException("Staff not at agency")

    return staff
  }

  companion object {
    const val STAFF_LOCATION_ROLE_CREATED = "NURA-staff-location-role-created"
    const val STAFF_LOCATION_ROLE_UPDATED = "NURA-staff-location-role-updated"
  }
}

fun StaffLocationRole.asStaffJobClassification() = StaffJobClassification(agencyId, staffId, role, position, scheduleType, hoursPerWeek, fromDate, toDate)

data class StaffNotFoundException(val staffId: Long) : RuntimeException("Staff member $staffId not found")
data class ReferenceCodes(val staffRole: ReferenceData, val position: ReferenceData, val scheduleType: ReferenceData)

fun StaffLocationRole.telemetryProperties(): Map<String, String> = listOfNotNull(
  "agencyId" to agencyId,
  "staffId" to "$staffId",
  "role" to role,
  "position" to position,
  "fromDate" to ISO_DATE.format(fromDate),
  toDate?.let { "toDate" to ISO_DATE.format(it) },
).toMap()
