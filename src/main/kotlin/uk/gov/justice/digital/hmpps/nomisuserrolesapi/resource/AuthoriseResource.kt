package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.ReportingAuthorisation
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.ReportingAuthorisationService

@RestController
@Validated
@RequestMapping("/authorise", produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthoriseResource(
  private val authenticationFacade: AuthenticationFacade,
  private val reportingAuthorisationService: ReportingAuthorisationService
) {

  @PostMapping("/reporting")
  @Operation(
    summary = "Authorise access to reporting system - WORK IN PROGRESS",
    description = "Authorised access to the Business Objects Reporting System. Caseloads are checked for certain NOMIS roles",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Role Information Returned"
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access reporting systems",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "503",
        description = "Reporting service is currently unavailable",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun authoriseReporting(): ReportingAuthorisation =
    authenticationFacade.currentUsername?.let { reportingAuthorisationService.tryGetAuthorisedUrl(it) }
      ?: throw AccessDeniedException("No username found in token")
}
