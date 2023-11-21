package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Linking general account user creation")
data class CreateLinkedGeneralUserRequest(
  @Schema(description = "Username", example = "TESTUSER1", required = true)
  @field:Size(
    max = 30,
    min = 1,
    message = "Username must be between 1 and 30",
  )
  @NotBlank
  val username: String,

  @Schema(
    description = "Default caseload (a.k.a Prison ID), not required for admin accounts",
    example = "BXI",
    required = true,
  )
  @field:Size(
    max = 6,
    min = 3,
    message = "Caseload must be between 3-6 characters",
  )
  @NotBlank
  val defaultCaseloadId: String,

)
