package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
  private val userService: UserService,
  private val authenticationFacade: AuthenticationFacade,
) {

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @GetMapping("/{username}")
  @Operation(
    summary = "Get specified user details",
    description = "Information on a specific user",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User Information Returned",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = UserDetail::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get user information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Username not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getUserDetails(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "username must be between 1 and 30") username: String
  ): UserDetail =
    userService.findByUsername(username)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @GetMapping
  @Operation(
    summary = "Get all users filtered has specified",
    description = "Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES. <br/>Get all users with filter.<br/> For local administrators this will implicitly filter users in the prison's they administer, therefore username is expected in the authorisation token. <br/>For users with role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN this allows access to all staff.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Pageable list of user summaries",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect filter supplied",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getUsers(
    @PageableDefault
    pageRequest: Pageable,
    @RequestParam(value = "nameFilter", required = false)
    @Schema(
      description = "Filter results by first name and/or username and/or last name of staff member",
      example = "Raj"
    )
    nameFilter: String?,
    @RequestParam(value = "accessRoles", required = false)
    @Schema(
      description = "Filter will match users that have all DPS role specified",
      example = "ADD_SENSITIVE_CASE_NOTES"
    )
    accessRoles: List<String>?,
    @RequestParam(value = "status", required = false, defaultValue = "ALL")
    @Schema(
      description = "Limit to active / inactive / show all users.",
      allowableValues = ["ACTIVE", "INACTIVE", "ALL"],
      defaultValue = "ALL",
      example = "INACTIVE"
    )
    status: UserStatus = UserStatus.ACTIVE,
    @Schema(
      description = "Filter results by user's currently active caseload i.e. the one they have currently selected",
      example = "MDI"
    )
    @RequestParam(value = "activeCaseload", required = false) activeCaseload: String?,
    @Schema(
      description = "Filter results to include only those users that have access to the specified caseload (irrespective of whether it is currently active or not",
      example = "MDI"
    )
    @RequestParam(value = "caseload", required = false) caseload: String?,
  ): Page<UserSummary> {
    return userService.getLocalUsers(
      pageRequest,
      authenticationFacade.currentUsername ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
    )
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Information")
data class UserDetail(
  @Schema(description = "Username", example = "testuser1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,
  @Schema(description = "Staff ID", example = "324323", required = true) @NotBlank val staffId: Long,
  @Schema(description = "First name of the user", example = "John", required = false) @field:Size(
    max = 35,
    min = 1,
    message = "First name must be between 1 and 35"
  ) val firstName: String,
  @Schema(description = "Last name of the user", example = "Smith", required = false) @field:Size(
    max = 35,
    min = 1,
    message = "First name must be between 1 and 35"
  ) val lastName: String,
)
