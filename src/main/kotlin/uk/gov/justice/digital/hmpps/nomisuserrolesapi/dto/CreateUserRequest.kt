package uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Information creation")
data class CreateUserRequest(
    @Schema(description = "Username", example = "testuser1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,
    @Schema(description = "Password", example = "password123", required = true) @field:Size(
        max = 30,
        min = 1,
        message = "password must be between 1 and 30"
    ) @NotBlank val password: String,
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