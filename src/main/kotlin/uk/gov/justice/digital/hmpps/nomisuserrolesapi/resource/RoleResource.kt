package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UpdateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.RoleService

@RestController
@Validated
@RequestMapping("/roles", produces = [MediaType.APPLICATION_JSON_VALUE])
class RoleResource(
  private val roleService: RoleService,
) {

  @PreAuthorize("hasAnyRole('ROLE_ROLES_ADMIN','ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create a role in NOMIS",
    description = "Creates a role. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CreateRoleRequest::class),
        ),
      ],
    ),
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Role Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to create role information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to create a role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun createRole(
    @RequestBody @Valid
    createRoleRequest: CreateRoleRequest,
  ): RoleDetail = roleService.createRole(createRoleRequest)

  @PreAuthorize("hasAnyRole('ROLE_ROLES_ADMIN','ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @PutMapping("/{code}")
  @Operation(
    summary = "Updates a role in NOMIS",
    description = "Updates a role. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = UpdateRoleRequest::class),
        ),
      ],
    ),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Role Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to update role information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to update a role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun updateRole(
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "Role code must be between 1 and 30")
    code: String,
    @RequestBody @Valid
    updateRoleRequest: UpdateRoleRequest,
  ): RoleDetail = roleService.updateRole(code, updateRoleRequest)

  @PreAuthorize("hasAnyRole('ROLE_ROLES_ADMIN','ROLE_MAINTAIN_ACCESS_ROLES_ADMIN', 'ROLE_MAINTAIN_ACCESS_ROLES', 'ROLE_VIEW_NOMIS_STAFF_DETAILS')")
  @GetMapping
  @Operation(
    summary = "Get all roles",
    description = "Information on a list of roles. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN, ROLE_MAINTAIN_ACCESS_ROLES or ROLE_VIEW_NOMIS_STAFF_DETAILS or ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Role Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get role information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a list of roles",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getAllRoles(
    @Parameter(
      description = "Get all roles, which includes both DPS and NOMIS roles",
      example = "true",
    )
    @RequestParam(value = "all-roles", required = false, defaultValue = "false")
    allRoles: Boolean = false,
    @Parameter(
      description = "Include DPS roles that can only be allocated by Central Admin",
      example = "true",
    )
    @RequestParam(value = "admin-roles", required = false, defaultValue = "true")
    adminRoles: Boolean = true,
  ): List<RoleDetail> = if (allRoles) {
    roleService.getAllRoles()
  } else {
    roleService.getAllDPSRoles(adminRoles)
  }

  @PreAuthorize("hasAnyRole('ROLE_ROLES_ADMIN','ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @GetMapping("/{code}")
  @Operation(
    summary = "Get specified role details",
    description = "Information on a specific role. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN or ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Role Information Returned",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to get role information",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to get a role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getRoleDetails(
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "Role code must be between 1 and 30")
    code: String,
  ): RoleDetail = roleService.findByCode(code)

  @PreAuthorize("hasRole('ROLE_DELETE ROLES_ADMIN')")
  @DeleteMapping("/{roleCode}")
  @Hidden
  @Operation(
    summary = "Delete Role",
    description = "Delete Role. Requires role ROLE_DELETE_ROLES_ADMIN",
    security = [SecurityRequirement(name = "DELETE_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to delete role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to delete a role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteRole(
    @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "role code must be between 1 and 30")
    roleCode: String,
  ) = roleService.deleteRole(roleCode)
}
