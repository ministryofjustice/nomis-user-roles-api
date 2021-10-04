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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ResponseStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto.CreateUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
  private val userService: UserService
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

  fun createUser(@RequestBody @Valid createUserRequest: CreateUserRequest
  ): UserDetail =
    userService.createUser(createUserRequest.username, createUserRequest.password, createUserRequest.firstName, createUserRequest.lastName)

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
}

