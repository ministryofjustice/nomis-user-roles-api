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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail

import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/roles", produces = [MediaType.APPLICATION_JSON_VALUE])
class RoleResource {

    @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a role in NOMIS",
        description = "Creates a role",
        security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CreateRoleRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Role Information Returned"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Incorrect request to create role information",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized to access this endpoint",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Incorrect permissions to create a role",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )

    fun createUser(
        @RequestBody @Valid creatRoleRequest: CreateRoleRequest
    ): RoleDetail {
        val role = roleService.createUser(createRoleRequest)
        return roleService.findByCode(roleCode = role.code)
    }

    @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
    @GetMapping("/{code}")
    @Operation(
        summary = "Get specified role details",
        description = "Information on a specific role",
        security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Role Information Returned"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Incorrect request to get role information",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized to access this endpoint",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Incorrect permissions to get a role",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getUserDetails(
        @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
        @PathVariable @Size(max = 30, min = 1, message = "Role code must be between 1 and 30") roleCode: String
    ): RoleDetail =
        roleService.findByCode(roleCode)

}