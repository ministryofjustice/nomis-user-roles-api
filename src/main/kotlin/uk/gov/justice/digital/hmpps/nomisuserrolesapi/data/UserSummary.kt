package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Summary User Information")
data class UserSummary(
  @Schema(description = "Username", example = "testuser1")
  val username: String,
  @Schema(description = "Staff ID", example = "324323")
  val staffId: Long,
  @Schema(description = "First name of the user", example = "Mustafa")
  val firstName: String,
  @Schema(description = "Last name of the user", example = "Usmani")
  val lastName: String,
  @Schema(description = "Account status indicator", example = "trie")
  val active: Boolean,
  val activeCaseload: PrisonCaseload?,
)
