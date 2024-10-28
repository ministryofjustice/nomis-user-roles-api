package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User last name")
data class UserLastName(
  @Schema(description = "Username", example = "testuser1")
  val username: String,
  @Schema(description = "Last name of the user", example = "Usmani")
  val lastName: String,
)
