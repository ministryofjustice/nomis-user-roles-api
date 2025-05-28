package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.JobClassificationRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffJobClassification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.StaffMemberService

@RestController
@RequestMapping("/agency/{agencyId}/staff-members")
class StaffMemberResource(private val staffMemberService: StaffMemberService) {
  @Operation(
    summary = "Create or update a job classification for a staff member",
    description = "Creates a staff location role if one doesn't exist and expires duplicates if multiples exist. Requires role ROLE_NOMIS_MANAGE_USERS__STAFF_MEMBERS__RW",
    security = [SecurityRequirement(name = "ROLE_NOMIS_MANAGE_USERS__STAFF_MEMBERS__RW")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = JobClassificationRequest::class),
        ),
      ],
    ),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The job classification was created or updated successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to modify the staff job classification",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions modify staff job classifications",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PutMapping("/{staffId}/staff-role/{staffRole}")
  @PreAuthorize("hasRole('ROLE_NOMIS_MANAGE_USERS__STAFF_MEMBERS__RW')")
  fun setJobClassification(
    @PathVariable agencyId: String,
    @PathVariable staffId: Long,
    @PathVariable staffRole: String,
    @RequestBody @Valid jobClassification: JobClassificationRequest,
  ): StaffJobClassification = staffMemberService.setJobClassification(agencyId, staffId, staffRole, jobClassification)
}
