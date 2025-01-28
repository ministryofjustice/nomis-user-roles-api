package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
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
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleAssignmentStats
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleAssignmentsSpecification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DPS_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.RoleAssignmentsService
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserRoleManagementResource(
  private val userService: UserService,
  private val roleAssignmentsService: RoleAssignmentsService,
) {

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES') or hasRole('ROLE_VIEW_NOMIS_STAFF_DETAILS')")
  @GetMapping("/{username}/roles")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Get list of roles associated with the users account",
    description = "Roles for a specific user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES or ROLE_VIEW_NOMIS_STAFF_DETAILS",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User role list",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get caseloads for a user",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get roles for this user",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getUserRoles(
    @Schema(description = "Username", example = "TEST_USER1", required = true)
    @PathVariable
    username: String,
    @Schema(
      name = "include-nomis-roles",
      description = "Include NOMIS roles",
      example = "false",
      required = false,
      defaultValue = "false",
    )
    @RequestParam(
      name = "include-nomis-roles",
      required = false,
      defaultValue = "false",
    )
    includeNomisRoles: Boolean = false,
  ): UserRoleDetail = userService.getUserRoles(username, includeNomisRoles)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PostMapping("/{username}/roles/{roleCode}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add a role to the specified user account, all roles will be added to DPS caseload unless specified",
    description = "Adds a role to a user, user must have caseload (if specified). Default caseload is DPS caseload (NWEB).  Cannot add an existing role to the same user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "User information with role details",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to add a role to a user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to add a role to this account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addRole(
    @Schema(
      description = "Username of the account to add role",
      example = "TEST_USER2",
      required = true,
    )
    @PathVariable
    @Size(max = 30, min = 1, message = "Username must be between 1 and 30")
    username: String,
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "Role code must be between 1 and 30")
    roleCode: String,
    @Schema(description = "Caseload Id", example = "NWEB", required = false, defaultValue = "NWEB")
    @RequestParam(required = false, defaultValue = "NWEB")
    @Size(
      max = 6,
      min = 3,
      message = "Caseload must be between 3-6 characters",
    )
    caseloadId: String = DPS_CASELOAD,
  ): UserRoleDetail = userService.addRoleToUser(username.uppercase(), roleCode, caseloadId)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PostMapping("/{username}/roles")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Add a role to the specified user account, all roles will be added to DPS caseload unless specified",
    description = "Adds a role to a user, user must have caseload (if specified). Default caseload is DPS caseload (NWEB).  Cannot add an existing role to the same user. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "User information with role details",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to add a role to a user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to add a role to this account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addRoles(
    @Schema(
      description = "Username of the account to add roles",
      example = "TEST_USER2",
      required = true,
    )
    @PathVariable
    @Size(max = 30, min = 1, message = "Username must be between 1 and 30 characters")
    username: String,
    @Schema(description = "Caseload Id", example = "NWEB", required = false, defaultValue = "NWEB")
    @RequestParam(required = false, defaultValue = "NWEB")
    @Size(
      max = 6,
      min = 3,
      message = "Caseload must be between 3-6 characters",
    )
    caseloadId: String = DPS_CASELOAD,
    @Schema(description = "Role Codes", required = true)
    @RequestBody
    @Valid
    roleCodes: List<String>,
  ): UserRoleDetail = userService.addRolesToUser(username, roleCodes, caseloadId)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @DeleteMapping("/{username}/roles/{roleCode}")
  @Operation(
    summary = "Remove a role from a user",
    description = "The user must already have the role to be removed. Default role caseload is a DPS role unless specified. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User information with role details",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to remove a role from a user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to remove a role this user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun removeRole(
    @Schema(
      description = "Username of the account to remove role",
      example = "TEST_USER2",
      required = true,
    )
    @PathVariable
    @Size(max = 30, min = 1, message = "Username must be between 1 and 30")
    username: String,
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "Role code must be between 1 and 30")
    roleCode: String,
    @Schema(description = "Caseload Id", example = "NWEB", required = false, defaultValue = "NWEB")
    @RequestParam(required = false, defaultValue = "NWEB")
    @Size(
      max = 6,
      min = 3,
      message = "Caseload must be between 3-6 characters",
    )
    caseloadId: String = DPS_CASELOAD,
  ): UserRoleDetail = userService.removeRoleFromUser(username, roleCode, caseloadId)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PostMapping("/remove-roles/{roleCode}")
  @Operation(
    summary = "Bulk removes a role from a group of users",
    description = "If the user does not have the role already it is ignored. Any users not found will also be ignored but will not be returned in the response. Only DPS roles are removed on the DPS caseload (NWEB). Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User information with role details",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to remove a role from a user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to remove a role this user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun bulkRemoveRoles(
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "Role code must be between 1 and 30")
    roleCode: String,
    @Schema(description = "Users", example = "JSMITH_GEN,JMOHMAND_GEN", required = true)
    @RequestBody
    users: String,
  ): List<UserRoleDetail> = userService.removeRoleFromUsers(users.asList(), roleCode)

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN') or hasRole('ROLE_MAINTAIN_ACCESS_ROLES')")
  @PostMapping("/add-roles/{roleCode}")
  @Operation(
    summary = "Bulk add a role to a group of users",
    description = "If the user has this role already it is ignored. Any users not found will also be ignored but will not be returned in the response. Only DPS roles are added to the DPS caseload (NWEB). Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLE_MAINTAIN_ACCESS_ROLES",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES"), SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User information with role details",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to add a role from a user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to add a role this user account",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun bulkAddRoles(
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "Role code must be between 1 and 30")
    roleCode: String,
    @Schema(description = "Users", example = "JSMITH_GEN,JMOHMAND_GEN", required = true)
    @RequestBody
    users: String,
  ): List<UserRoleDetail> = userService.addRoleToUsers(users.asList(), roleCode)

  @PostMapping("/reassign-roles")
  @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES_ADMIN')")
  @Operation(
    summary = "Reassign roles from a NOMIS role to a DPS role and removes the NOMIS role if no longer required",
    description = "Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Role update details",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to re-assign a set of roles",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to re-assign a set of roles",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun reassignRoles(
    @Valid @RequestBody
    specification: RoleAssignmentsSpecification,
  ): List<RoleAssignmentStats> = roleAssignmentsService.updateRoleAssignments(specification)
}

private fun String.asList(): List<String> = this.split(",").map { it.removeQuotes().trim() }

private fun String.removeQuotes(): String = this
  .replace("\"", "")
  .replace("'", "")
