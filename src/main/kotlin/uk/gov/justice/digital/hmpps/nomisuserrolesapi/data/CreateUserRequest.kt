package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Information creation")
data class CreateUserRequest(
  @Schema(description = "Username", example = "TESTUSER1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30"
  ) @NotBlank val username: String,
  @Schema(description = "Password", example = "password123", required = true) @field:Size(
    max = 30,
    min = 9,
    message = "Password must be at least 9 alpha-numeric characters in length (max 30). Please re-enter password."
  ) @field:Pattern(regexp = "^[A-Za-z0-9]*\$", message = "Password must consist of alphanumeric characters only")
  @NotBlank val password: String,
  @Schema(description = "First name of the user, required if linkedUsername is not set", example = "John", required = false) @field:Size(
    max = 35,
    min = 1,
    message = "First name must be between 1 and 35"
  ) val firstName: String? = null,
  @Schema(description = "Last name of the user, required if linkedUsername is not set", example = "Smith", required = false) @field:Size(
    max = 35,
    min = 1,
    message = "First name must be between 1 and 35"
  ) val lastName: String? = null,

  @Schema(description = "Default caseload (a.k.a Prison ID)", example = "BXI", required = true) @field:Size(
    max = 6,
    min = 3,
    message = "Caseload must be between 3-6 characters"
  ) @NotBlank val defaultCaseloadId: String,

  @Schema(description = "Email Address, required if linkedUsername is not set", example = "test@justice.gov.uk", required = false) @field:Email(
    message = "Not a valid email address"
  ) val email: String? = null,

  @Schema(description = "Admin User?", example = "true", required = false, defaultValue = "false")
  val adminUser: Boolean = false,

  @Schema(description = "Attach account to an existing staff record by existing user account", example = "TESTUSER2", required = false)
  val linkedUsername: String? = null
)
