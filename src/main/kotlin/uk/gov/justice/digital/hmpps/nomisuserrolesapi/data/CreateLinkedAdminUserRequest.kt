package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Linking admin account user creation")
data class CreateLinkedAdminUserRequest(
  @Schema(description = "Username", example = "TESTUSER1", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "Username must be between 1 and 30"
  ) @NotBlank val username: String,

)
