package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
  private val userService: UserService,
  private val authenticationFacade: AuthenticationFacade,
) {
  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create user",
    description = "Create user",
    security = [SecurityRequirement(name = "CREATE_USER")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CreateUserRequest::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "User Information Returned",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = UserDetail::class))]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to create user information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to create a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )

  fun createUser(
    @RequestBody @Valid createUserRequest: CreateUserRequest
  ): UserDetail =
    userService.createUser(createUserRequest)

  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @DeleteMapping("/{username}")
  @Operation(
    summary = "Delete user",
    description = "Delete user",
    security = [SecurityRequirement(name = "CREATE_USER")],
    responses = [
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to delete user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to delete a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )

  fun deleteUser(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "username must be between 1 and 30") username: String
  ) =
    userService.deleteUser(username)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @GetMapping("/{username}")
  @Operation(
    summary = "Get specified user details",
    description = "Information on a specific user",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
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
  ): Page<UserSummary> = userService.findUsersByFilter(
    pageRequest,
    UserFilter(
      localAdministratorUsername = localAdministratorUsernameWhenNotCentralAdministrator(),
      name = if (nameFilter.isNullOrBlank()) null else nameFilter,
      status = status,
    )
  )

  fun localAdministratorUsernameWhenNotCentralAdministrator(): String? =
    if (AuthenticationFacade.hasRoles("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")) null else authenticationFacade.currentUsername
}
