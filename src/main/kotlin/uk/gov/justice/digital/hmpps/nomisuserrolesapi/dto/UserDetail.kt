package uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

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
) {
  constructor(userPersonDetail: UserPersonDetail) :
      this(username = userPersonDetail.username,
        staffId = userPersonDetail.staff.staffId,
        firstName = userPersonDetail.staff.firstName,
        lastName = userPersonDetail.staff.lastName)
}