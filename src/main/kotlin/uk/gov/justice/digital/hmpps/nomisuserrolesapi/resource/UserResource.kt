package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.Valid
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
  private val userService: UserService,
  private val authenticationFacade: AuthenticationFacade,
) {
  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @PostMapping("/general-account")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create general user account",
    description = "Creates general user account, oracle schema and staff user information. Requires role ROLE_CREATE_USER",
    security = [SecurityRequirement(name = "CREATE_USER")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CreateGeneralUserRequest::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "General user information returned"
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
  fun createGeneralUser(
    @RequestBody @Valid createUserRequest: CreateGeneralUserRequest
  ): UserDetail {
    val user = userService.createGeneralUser(createUserRequest)
    return userService.findByUsername(username = user.username)
  }

  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @PostMapping("/admin-account")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create admin user account",
    description = "Creates admin user account, oracle schema and staff user information. Requires role ROLE_CREATE_USER",
    security = [SecurityRequirement(name = "CREATE_USER")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CreateAdminUserRequest::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Admin user account information returned"
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
  fun createAdminUser(
    @RequestBody @Valid createUserRequest: CreateAdminUserRequest
  ): UserDetail {
    val user = userService.createAdminUser(createUserRequest)
    return userService.findByUsername(username = user.username)
  }

  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @PostMapping("/link-general-account/{linkedUsername}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Link a general user account to an existing admin account.",
    description = "Can only be linked to an admin account. Can only be linked to an account that doesn't already have one general account. Requires role ROLE_CREATE_USER",
    security = [SecurityRequirement(name = "CREATE_USER")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CreateLinkedGeneralUserRequest::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Staff account information returned"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to link general account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to link a general account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun linkGeneralAccount(
    @Schema(description = "Attach account to an existing admin user account", example = "testuser2", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") linkedUsername: String,
    @RequestBody @Valid linkedGeneralUserRequest: CreateLinkedGeneralUserRequest
  ): StaffDetail {
    val linkedUser = userService.linkGeneralAccount(linkedUsername, linkedGeneralUserRequest)
    return userService.findByStaffId(linkedUser.staffId)
  }

  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @PostMapping("/link-admin-account/{linkedUsername}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Link an admin account to an existing general account.",
    description = "Can only be linked to an general account. Can only be linked to an account that doesn't already have one Admin account. Requires role ROLE_CREATE_USER",
    security = [SecurityRequirement(name = "CREATE_USER")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CreateLinkedAdminUserRequest::class)
        )
      ]
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Staff account information returned"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to link admin account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to link an admin account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun linkAdminAccount(
    @Schema(description = "Attach account to an existing general account", example = "testuser2", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") linkedUsername: String,
    @RequestBody @Valid linkedUserRequest: CreateLinkedAdminUserRequest
  ): StaffDetail {
    val linkedUser = userService.linkAdminAccount(linkedUsername, linkedUserRequest)
    return userService.findByStaffId(linkedUser.staffId)
  }

  @PreAuthorize("hasRole('ROLE_CREATE_USER')")
  @DeleteMapping("/{username}")
  @Hidden
  @Operation(
    summary = "Delete user from system",
    description = "Removes user and staff related data, along with roles and caseloads. Also removed oracle schema user. Requires role ROLE_CREATE_USER",
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
    description = "Information on a specific user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
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
  fun getUserDetails(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "username must be between 1 and 30") username: String
  ): UserDetail =
    userService.findByUsername(username)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @GetMapping("/staff/{staffId}")
  @Operation(
    summary = "Get specified staff details",
    description = "Will display general and admin user account if setup.  Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Staff Information Returned"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get staff information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a staff user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getUserDetailsByStaffId(
    @Schema(description = "Staff ID", example = "234232", required = true) @PathVariable staffId: Long
  ): StaffDetail =
    userService.findByStaffId(staffId)

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
    @PageableDefault(sort = ["lastName", "firstName"], direction = Sort.Direction.ASC)
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
      name = nameFilter.nonBlank(),
      status = status,
      activeCaseloadId = activeCaseload.nonBlank(),
      caseloadId = caseload.nonBlank(),
      roleCodes = accessRoles ?: listOf(),
    ),
  )

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @PutMapping("/{username}/lock-user")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Lock user account",
    description = "Locks the user account. Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    security = [SecurityRequirement(name = "MANAGE_NOMIS_USER_ACCOUNT")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User account locked"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to lock user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to lock a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun lockUser(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "username must be between 1 and 30") username: String
  ) {
    userService.lockUser(username)
  }

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @PutMapping("/{username}/unlock-user")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Unlock user account",
    description = "Unlocks the user account. Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    security = [SecurityRequirement(name = "MANAGE_NOMIS_USER_ACCOUNT")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User account unlocked"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to unlock user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to unlock a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun unlockUser(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "username must be between 1 and 30") username: String
  ) {
    userService.unlockUser(username)
  }

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @PutMapping("/{username}/change-password")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Change password of user account",
    description = "Change password of user account. Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    security = [SecurityRequirement(name = "MANAGE_NOMIS_USER_ACCOUNT")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User account password changed"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to change password of user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to change the password a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun changePassword(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "username must be between 1 and 30") username: String,
    @Schema(description = "Password", example = "HeLl0W0R1D", required = true)
    @Pattern(regexp = "^[A-Za-z0-9]{14,30}$", message = "Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars")
    @RequestBody @Valid password: String,
  ) {
    userService.changePassword(username, password)
  }

  fun localAdministratorUsernameWhenNotCentralAdministrator(): String? =
    if (AuthenticationFacade.hasRoles("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")) null else authenticationFacade.currentUsername
}

private fun String?.nonBlank() = if (this.isNullOrBlank()) null else this
