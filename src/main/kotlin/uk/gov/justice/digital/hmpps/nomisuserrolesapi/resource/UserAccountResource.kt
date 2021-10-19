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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserAccountResource(
  private val userService: UserService,
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
  @PostMapping("/local-admin-account")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create local admin user account",
    description = "Creates local admin user account, oracle schema and staff user information. Requires role ROLE_CREATE_USER",
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
        description = "Local Admin user information returned"
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
  fun createLocalAdminUser(
    @RequestBody @Valid createLocalAdminUserRequest: CreateGeneralUserRequest
  ): UserDetail {
    val user = userService.createLocalAdminUser(createLocalAdminUserRequest)
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
  @PostMapping("/link-local-admin-account/{linkedUsername}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Link a local admin user account to an existing general account.",
    description = "Can only be linked to an general account. Can only be linked to an account that doesn't already have one admin account. Requires role ROLE_CREATE_USER",
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
        description = "Staff local admin account information returned"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to link local admin account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to link a local admin account",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun linkLocalAdminAccount(
    @Schema(description = "Attach account to an existing general user account", example = "testuser2", required = true)
    @PathVariable @Size(max = 30, min = 1, message = "Username must be between 1 and 30") linkedUsername: String,
    @RequestBody @Valid linkedLocalAdminUserRequest: CreateLinkedGeneralUserRequest
  ): StaffDetail {
    val linkedUser = userService.linkLocalAdminAccount(linkedUsername, linkedLocalAdminUserRequest)
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
}
