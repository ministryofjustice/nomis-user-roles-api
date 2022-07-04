package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserCaseloadDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserCaseloadManagementResource(
  private val userService: UserService
) {

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @GetMapping("/{username}/caseloads")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Get list of caseloads associated with the users account",
    description = "Caseloads for a specific user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
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
  fun getUserCaseloads(
    @Schema(description = "Username", example = "TEST_USER1", required = true)
    @PathVariable username: String
  ): UserCaseloadDetail {
    return userService.getCaseloads(username)
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PostMapping("/{username}/caseloads/{caseloadId}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add a caseload to the specified user account",
    description = "Adds a caseload to a user, caseload must exist. Cannot add an existing caseload to the same user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "User information with caseload details"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to add a caseload to a user account",
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
        description = "Incorrect permissions to add a caseload to account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  fun addCaseload(
    @Schema(
      description = "Username of the account to add caseload",
      example = "TEST_USER2",
      required = true
    )
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") username: String,
    @Schema(description = "Caseload Id", example = "LEI", required = true)
    @PathVariable @Size(max = 6, min = 3, message = "Caseload must be between 3 and 6") caseloadId: String,
  ): UserCaseloadDetail {
    return userService.addCaseloadToUser(username, caseloadId)
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PostMapping("/{username}/caseloads")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add multiple caseloads to the specified user account",
    description = "Adds caseloads to a user, caseloads must exist. Cannot add an existing caseload to the same user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "User information with caseload details"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to add caseloads to a user account",
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
        description = "Incorrect permissions to add caseloads to account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  fun addCaseloads(
    @Schema(
      description = "Username of the account to add caseloads",
      example = "TEST_USER2",
      required = true
    )
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") username: String,
    @RequestBody @Valid caseloadIds: List<String>,
  ): UserCaseloadDetail {
    return userService.addCaseloadsToUser(username, caseloadIds)
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PutMapping("/{username}/default-caseload/{defaultCaseloadId}")
  @Operation(
    summary = "Set the default caseload for this user",
    description = "Sets the default caseload. Caseload must already be present. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User information with active caseload details"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to set a default caseload on a user account",
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
        description = "Incorrect permissions to set a default caseload on a user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  fun setDefaultCaseload(
    @Schema(
      description = "Username to default caseload",
      example = "TEST_USER2",
      required = true
    )
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") username: String,
    @Schema(description = "Default caseload Id", example = "LEI", required = true)
    @PathVariable @Size(max = 6, min = 3, message = "Caseload must be between 3 and 6") defaultCaseloadId: String,
  ): UserCaseloadDetail {
    return userService.setDefaultCaseload(username, defaultCaseloadId)
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @DeleteMapping("/{username}/caseloads/{caseloadId}")
  @Operation(
    summary = "Remove a caseload from a user",
    description = "The user must already have the caseload to be removed. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User information with caseload details"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to remove a caseload from a user account",
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
        description = "Incorrect permissions to remove a caseload this user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  fun removeCaseload(
    @Schema(
      description = "Username to remove caseload from",
      example = "TEST_USER2",
      required = true
    )
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") username: String,
    @Schema(description = "Caseload ID to remove from this user", example = "LEI", required = true)
    @PathVariable @Size(max = 6, min = 3, message = "Caseload must be between 3 and 6") caseloadId: String,
  ): UserCaseloadDetail {
    return userService.removeCaseload(username, caseloadId)
  }
}
