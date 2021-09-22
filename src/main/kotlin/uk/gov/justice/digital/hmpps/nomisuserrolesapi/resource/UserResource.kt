package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
  private val userService: UserService
) {

  @PreAuthorize("hasRole('ROLE_USER_ADMIN')")
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
