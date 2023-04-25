package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Local Admin User Information creation")
data class CreateLocalAdminUserRequest(
  @Schema(description = "Username", example = "TESTUSER1", required = true)
  @field:Size(
    max = 30,
    min = 1,
    message = "username must be between 1 and 30",
  )
  @NotBlank
  val username: String,
  @Schema(
    description = "First name of the user, required if linkedUsername is not set",
    example = "John",
    required = true,
  )
  @field:Pattern(
    regexp = "^[A-Za-z'-]{1,35}$",
    message = "First name must consist of alphabetical characters, a hyphen or an apostrophe only and a max 35 chars",
  )
  val firstName: String,
  @Schema(
    description = "Last name of the user, required if linkedUsername is not set",
    example = "Smith",
    required = true,
  )
  @field:Pattern(
    regexp = "^[A-Za-z'-]{1,35}$",
    message = "Last name must consist of alphabetical characters, a hyphen or an apostrophe only and a max 35 chars",
  )
  val lastName: String,

  @Schema(
    description = "Email Address, required if linkedUsername is not set",
    example = "test@justice.gov.uk",
    required = true,
  )
  @field:Email(
    message = "Not a valid email address",
  )
  val email: String,

  @Schema(
    description = "Default local admin group (prison) to manage users",
    example = "MDI",
    required = true,
  )
  @field:Size(
    max = 6,
    min = 3,
    message = "Admin group must be between 3-6 characters",
  )
  @NotBlank
  val localAdminGroup: String,
)
