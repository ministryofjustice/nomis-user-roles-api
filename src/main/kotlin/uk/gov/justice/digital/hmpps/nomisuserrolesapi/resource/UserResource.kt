package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummaryWithEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserAndEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
  private val userService: UserService,
  private val authenticationFacade: AuthenticationFacade,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
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
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to delete a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteUser(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30")
    username: String,
  ) = userService.deleteUser(username)

  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN', 'ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @GetMapping("/{username}")
  @Operation(
    summary = "Get specified user details",
    description = "Information on a specific user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES or ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    security = [
      SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"),
      SecurityRequirement(
        name = "ROLE_MANAGE_NOMIS_USER_ACCOUNT",
      ),
    ],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get user information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getUserDetails(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    username: String,
  ): UserDetail {
    log.info("Fetching user details for : {}", username)
    val userDetail = userService.findByUsername(username)
    log.info("Returning user details for : {}", username)
    return userDetail
  }

  @PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN', 'ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @GetMapping("/basic/{username}")
  @Operation(
    summary = "Get basic user details",
    description = "Information on a specific user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES or ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    security = [
      SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"),
      SecurityRequirement(
        name = "ROLE_MANAGE_NOMIS_USER_ACCOUNT",
      ),
    ],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get user information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "user not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getBasicUserDetailsInfo(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    username: String,
  ) = userService.findBasicUserDetails(username)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @GetMapping("/staff/{staffId}")
  @Operation(
    summary = "Get specified staff details",
    description = "Will display general and admin user account if setup.  Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Staff Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get staff information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a staff user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getUserDetailsByStaffId(
    @Schema(description = "Staff ID", example = "234232", required = true) @PathVariable
    staffId: Long,
  ): StaffDetail =
    userService.findByStaffId(staffId)

  @PreAuthorize("hasAnyRole('ROLE_USE_OF_FORCE', 'ROLE_STAFF_SEARCH')")
  @GetMapping("/staff")
  @Operation(
    summary = "Find users by first and last names",
    description = "Requires role ROLE_USE_OF_FORCE or ROLE_STAFF_SEARCH",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "List of matching users",
      ),
    ],
  )
  fun findUsersByFirstAndLastNames(
    @Parameter(
      description = "The first name to match. Case insensitive.",
      example = "Fred",
    ) @RequestParam @NotEmpty
    firstName: String,
    @Parameter(
      description = "The last name to match. Case insensitive",
      example = "Bloggs",
    ) @RequestParam @NotEmpty
    lastName: String,
  ): List<UserSummaryWithEmail> = userService.findUsersByFirstAndLastNames(firstName, lastName)

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @GetMapping("/user")
  @Operation(
    summary = "Find users by their email address",
    description = "Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "List of matching users",
      ),
    ],
  )
  fun findUsersByEmailAddress(
    @Parameter(
      description = "The email to match. Case insensitive",
      example = "jim@smith.com",
      required = true,
    ) @RequestParam @NotEmpty
    email: String,
  ): List<UserDetail> = userService.findAllByEmailAddress(email)

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @PostMapping("/user")
  @Operation(
    summary = "Find users by their email address and / or list of usernames",
    description = "Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "List of matching users",
      ),
    ],
  )
  fun findUsersByEmailAddressAndUsernames(
    @Parameter(
      description = "The email to match. Case insensitive",
      example = "jim@smith.com",
      required = true,
    ) @RequestParam @NotEmpty
    email: String,
    @Parameter(description = "List of usernames.") @RequestBody
    usernames: List<String>?,
  ): List<UserDetail> = userService.findAllByEmailAddressAndUsernames(email, usernames)

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @GetMapping("/emails")
  @Operation(
    summary = "Get all users",
    description = "Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "List of usernames and their email addresses",
      ),
    ],
  )
  fun findUsersAndEmails(): List<UserAndEmail> = userService.findUsersAndEmails()

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @GetMapping
  @Operation(
    summary = "Get all users filtered as specified",
    description = "Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES. <br/>Get all users with filter.<br/> For local administrators this will implicitly filter users in the prisons they administer, therefore username is expected in the authorisation token. <br/>For users with role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN this allows access to all staff.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Pageable list of user summaries",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect filter supplied",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getUsers(
    @PageableDefault(sort = ["lastName", "firstName"], direction = Sort.Direction.ASC)
    pageRequest: Pageable,
    @RequestParam(value = "nameFilter", required = false)
    @Parameter(
      description = "Filter results by name (first name and/or last name in any order), username or email address.",
      example = "Raj",
    )
    nameFilter: String?,
    @RequestParam(value = "accessRoles", required = false)
    @Parameter(
      description = "Filter will match users that have all DPS role specified",
      example = "ADD_SENSITIVE_CASE_NOTES",
    )
    accessRoles: List<String>?,
    @RequestParam(value = "nomisRole", required = false)
    @Parameter(
      description = "Filter will match users that have the NOMIS role specified, should be used with a caseloadId or will get duplicates",
      example = "201",
    )
    nomisRole: String?,
    @RequestParam(value = "status", required = false, defaultValue = "ALL")
    @Parameter(
      description = "Limit to active / inactive / show all users",
      example = "INACTIVE",
    )
    status: UserStatus = UserStatus.ACTIVE,
    @Parameter(
      description = "Filter results by user's currently active caseload i.e. the one they have currently selected",
      example = "MDI",
    )
    @RequestParam(value = "activeCaseload", required = false)
    activeCaseload: String?,
    @Parameter(
      description = "Filter results to include only those users that have access to the specified caseload (irrespective of whether it is currently active or not",
      example = "MDI",
    )
    @RequestParam(value = "caseload", required = false)
    caseload: String?,
    @RequestParam(value = "inclusiveRoles", required = false, defaultValue = "false")
    @Parameter(
      description = "Returns result inclusive of selected roles",
      example = "true",
    )
    inclusiveRoles: Boolean = false,
    @RequestParam(value = "showOnlyLSAs", required = false, defaultValue = "false")
    @Parameter(
      description = "Returns all active LSAs",
      example = "true",
    )
    showOnlyLSAs: Boolean = false,
  ): Page<UserSummaryWithEmail> = userService.findUsersByFilter(
    pageRequest,
    UserFilter(
      localAdministratorUsername = localAdministratorUsernameWhenNotCentralAdministrator(),
      name = nameFilter.nonBlank(),
      status = status,
      activeCaseloadId = activeCaseload.nonBlank(),
      caseloadId = caseload.nonBlank(),
      roleCodes = accessRoles ?: listOf(),
      nomisRoleCode = nomisRole,
      inclusiveRoles = inclusiveRoles,
      showOnlyLSAs = showOnlyLSAs,
    ),
  )

  @GetMapping("/download")
  fun downloadUsersByFilters(
    @PageableDefault(sort = ["lastName", "firstName"], direction = Sort.Direction.ASC)
    pageRequest: Pageable,
    @RequestParam(value = "nameFilter", required = false)
    @Parameter(
      description = "Filter results by name (first name and/or last name in any order), username or email address.",
      example = "Raj",
    )
    nameFilter: String?,
    @RequestParam(value = "accessRoles", required = false)
    @Parameter(
      description = "Filter will match users that have all DPS role specified",
      example = "ADD_SENSITIVE_CASE_NOTES",
    )
    accessRoles: List<String>?,
    @RequestParam(value = "nomisRole", required = false)
    @Parameter(
      description = "Filter will match users that have the NOMIS role specified, should be used with a caseloadId or will get duplicates",
      example = "201",
    )
    nomisRole: String?,
    @RequestParam(value = "status", required = false, defaultValue = "ALL")
    @Parameter(
      description = "Limit to active / inactive / show all users",
      example = "INACTIVE",
    )
    status: UserStatus = UserStatus.ACTIVE,
    @Parameter(
      description = "Filter results by user's currently active caseload i.e. the one they have currently selected",
      example = "MDI",
    )
    @RequestParam(value = "activeCaseload", required = false)
    activeCaseload: String?,
    @Parameter(
      description = "Filter results to include only those users that have access to the specified caseload (irrespective of whether it is currently active or not",
      example = "MDI",
    )
    @RequestParam(value = "caseload", required = false)
    caseload: String?,
    @RequestParam(value = "inclusiveRoles", required = false)
    @Parameter(
      description = "Returns result inclusive of selected roles",
      example = "true",
    )
    inclusiveRoles: Boolean?,
    @RequestParam(value = "showOnlyLSAs", required = false, defaultValue = "false")
    @Parameter(
      description = "Returns all active LSAs",
      example = "true",
    )
    showOnlyLSAs: Boolean = false,
  ): List<UserSummaryWithEmail> = userService.downloadUserByFilter(
    UserFilter(
      localAdministratorUsername = localAdministratorUsernameWhenNotCentralAdministrator(),
      name = nameFilter.nonBlank(),
      status = status,
      activeCaseloadId = activeCaseload.nonBlank(),
      caseloadId = caseload.nonBlank(),
      roleCodes = accessRoles ?: listOf(),
      nomisRoleCode = nomisRole,
      inclusiveRoles = inclusiveRoles,
      showOnlyLSAs = showOnlyLSAs,
    ),
  )

  fun localAdministratorUsernameWhenNotCentralAdministrator(): String? =
    if (AuthenticationFacade.hasRoles("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")) null else authenticationFacade.currentUsername
}

private fun String?.nonBlank() = if (this.isNullOrBlank()) null else this
