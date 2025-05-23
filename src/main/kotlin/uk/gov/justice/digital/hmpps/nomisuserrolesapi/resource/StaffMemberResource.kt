package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.JobClassificationRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffJobClassification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.StaffMemberService

@RestController
@RequestMapping("/agency/{agencyId}/staff-members")
class StaffMemberResource(private val staffMemberService: StaffMemberService) {
  @PutMapping("/{staffId}/staff-role/{staffRole}")
  @PreAuthorize("hasRole('ROLE_NOMIS_MANAGE_USERS__STAFF_MEMBERS__RW')")
  fun setJobClassification(
    @PathVariable agencyId: String,
    @PathVariable staffId: Long,
    @PathVariable staffRole: String,
    @RequestBody @Valid jobClassification: JobClassificationRequest,
  ): StaffJobClassification = staffMemberService.setJobClassification(agencyId, staffId, staffRole, jobClassification)
}
