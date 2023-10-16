package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserManagementResource(
  private val userService: UserService,
) {

  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @PutMapping("/{username}/logon-date")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Record the date and time of last logon for the user",
    description = "Record the date and time of last logon for the user. Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
    security = [SecurityRequirement(name = "MANAGE_NOMIS_USER_ACCOUNT")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Last logon date recorded",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to record last logon date",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to record the last logon date for the user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun recordLogonDate(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30")
    username: String,
  ) {
    userService.recordLogonDate(username)
  }

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
        description = "User account locked",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to lock user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to lock a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun lockUser(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30")
    username: String,
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
        description = "User account unlocked",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to unlock user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to unlock a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun unlockUser(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30")
    username: String,
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
        description = "User account password changed",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to change password of user. Some specific responses returns a different errorCode that can be checked by the client",
        content = [
          Content(
            mediaType = "application/json",
            examples = [
              ExampleObject(
                summary = "Password used before",
                name = "Password validation failed due to NOMIS matching the password with a previously used password",
                value = """
              {
                "status": 400,
                "errorCode": 1001,
                "userMessage": "Password has been used before and was rejected by NOMIS due to <reason>",
                "developerMessage": "Password has been used before and was rejected by NOMIS due to <reason>"
              }
            """,
              ),
              ExampleObject(
                summary = "Password extended validation failed",
                name = "Password extended validation failed due to NOMIS rejecting the password",
                value = """
              {
                "status": 400,
                "errorCode": 1002,
                "userMessage": "Password is not valid and has been rejected by NOMIS due to <reason>",
                "developerMessage": "Password is not valid and has been rejected by NOMIS due to <reason>"
              }
            """,
              ),
              ExampleObject(
                summary = "Password simple validation failed",
                name = "Password simple validation failed due to size and basic content",
                value = """
              {
                "status": 400,
                "errorCode": 1000,
                "userMessage": "Validation failure: changePassword.password: Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars",
                "developerMessage": "changePassword.password: Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars"
              }
            """,
              ),
            ],
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to change the password a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun changePassword(
    @Schema(description = "Username", example = "testuser1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30")
    username: String,
    @Schema(description = "Password", example = "HeLl0W0R1D", required = true)
    @RequestBody
    password: String,
  ) {
    userService.changePassword(username, password)
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @PutMapping("/{username}/change-email")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Change email of user account",
    description = "Change email of user account. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User account email changed",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to change email of user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to change the email a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun changeEmail(
    @Schema(description = "Username", example = "TEST_USER1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30 characters")
    username: String,
    @Schema(description = "Email", example = "test@test.com", required = true)
    @Email(message = "Invalid email address")
    @RequestBody
    @Valid
    email: String,
  ): StaffDetail {
    return userService.updateEmail(username, email)
  }

  @PreAuthorize("hasRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN')")
  @PutMapping("/{username}/change-name")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Change staff name of user account",
    description = "Change staff name of user account. Requires role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN",
    security = [SecurityRequirement(name = "MAINTAIN_ACCESS_ROLES_ADMIN")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User account name changed",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Incorrect request to change name of user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to change the name a user",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun changeName(
    @Schema(description = "Username", example = "TEST_USER1", required = true)
    @PathVariable
    @Size(max = 30, min = 1, message = "username must be between 1 and 30 characters")
    username: String,
    @Schema(description = "Staff name details", required = true) @RequestBody @Valid
    nameDetails: NameDetail,
  ): StaffDetail {
    return userService.updateStaffName(
      username = username,
      firstName = nameDetails.firstName,
      lastName = nameDetails.lastName,
    )
  }

  @Operation(
    summary = "Authenticate a username and password against NOMIS database",
    security = [SecurityRequirement(name = "ROLE_MANAGE_NOMIS_USER_ACCOUNT")],
    responses = [
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Authentication failed. Either no access token was provided or the username / password combination is incorrect",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Requires role ROLE_MANAGE_NOMIS_USER_ACCOUNT",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = Authentication::class),
        ),
      ],
    ),

  )
  @PreAuthorize("hasRole('ROLE_MANAGE_NOMIS_USER_ACCOUNT')")
  @PostMapping("{username}/authenticate")
  fun authenticate(
    @PathVariable username: String,
    @NotNull
    @RequestBody
    @Valid
    user: Authentication,
  ) {
    if (!userService.authenticateUser(username, user.password)) {
      throw UnauthorisedException("User with username $username failed authentication")
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Name Information")
data class NameDetail(
  @Schema(description = "First name of the user", example = "John", required = true)
  @field:Pattern(
    regexp = "^[A-Za-z'-]{1,35}$",
    message = "First name must consist of alphabetical characters, a hyphen or an apostrophe only and a max 35 chars",
  )
  val firstName: String,
  @Schema(description = "Last name of the user", example = "Smith", required = true)
  @field:Pattern(
    regexp = "^[A-Za-z'-]{1,35}$",
    message = "Last name must consist of alphabetical characters, a hyphen or an apostrophe only and a max 35 chars",
  )
  val lastName: String,
)

class UnauthorisedException(message: String) : Exception(message)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication Details")
data class Authentication(
  @Schema(description = "Password", example = "password123456", required = true)
  val password: @NotBlank String,
)
