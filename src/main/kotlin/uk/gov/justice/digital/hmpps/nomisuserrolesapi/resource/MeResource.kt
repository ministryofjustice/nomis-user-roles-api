package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserCaseloadDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserNotFoundException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

@RestController
@Validated
@RequestMapping("/me", produces = [MediaType.APPLICATION_JSON_VALUE])
class MeResource(
  private val userService: UserService,
  private val authenticationFacade: AuthenticationFacade
) {

  @GetMapping("")
  @Operation(
    summary = "Get user details in context",
    description = "Information on a specific user.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User Information Returned"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get user information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getMyUserDetails(): UserDetail =

    authenticationFacade.currentUsername?.run {
      return userService.findByUsername(this)
    } ?: throw UserNotFoundException("No user in context")

  @GetMapping("/caseloads")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Get list of caseloads associated with the current user",
    description = "Caseloads for the current user",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User caseload list"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get caseloads for a user",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a caseload for a user",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  fun getMyCaseloads(): UserCaseloadDetail {
    authenticationFacade.currentUsername?.run {
      return userService.getCaseloads(this)
    } ?: throw UserNotFoundException("No user in context")
  }

  @GetMapping("/roles")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Get list of roles associated with the users account",
    description = "Roles for a specific user in context",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User role list"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get caseloads for a user",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get roles for this user",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  fun getMyRoles(
    @Schema(name = "include-nomis-roles", description = "Include NOMIS roles", example = "false", required = false, defaultValue = "false")
    @RequestParam(name = "include-nomis-roles", required = false, defaultValue = "false") includeNomisRoles: Boolean = false,
  ): UserRoleDetail {
    authenticationFacade.currentUsername?.run {
      return userService.getUserRoles(this, includeNomisRoles)
    } ?: throw UserNotFoundException("No user in context")
  }
}
