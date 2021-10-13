package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Admin User Information creation")
data class CreateAdminUserRequest(
  @Schema(description = "Username", example = "TESTUSER1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,
  @Schema(description = "First name of the user, required if linkedUsername is not set", example = "John", required = true) @field:Size(
    max = 35,
    min = 1,
    message = "First name must be between 1 and 35"
  ) val firstName: String,
  @Schema(description = "Last name of the user, required if linkedUsername is not set", example = "Smith", required = true) @field:Size(
    max = 35,
    min = 1,
    message = "First name must be between 1 and 35"
  ) val lastName: String,

  @Schema(description = "Email Address, required if linkedUsername is not set", example = "test@justice.gov.uk", required = true) @field:Email(
    message = "Not a valid email address"
  ) val email: String,

)