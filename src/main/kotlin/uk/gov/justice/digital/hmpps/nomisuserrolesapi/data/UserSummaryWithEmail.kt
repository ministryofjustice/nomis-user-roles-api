package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Summary User Information with Email Address")
data class UserSummaryWithEmail(
  @Schema(description = "Username", example = "testuser1")
  val username: String,
  @Schema(description = "Staff ID", example = "324323")
  val staffId: Long,
  @Schema(description = "First name of the user", example = "Mustafa")
  val firstName: String,
  @Schema(description = "Last name of the user", example = "Usmani")
  val lastName: String,
  @Schema(description = "Account status indicator", example = "true")
  val active: Boolean,
  @Schema(description = "Caseload that is currently active, typically the prison the user is currently working at")
  val activeCaseload: PrisonCaseload?,
  @Schema(description = "Primary email address of user - normally justice.gov.uk one if available otherwise first one in list", example = "joe.bloggs@justice.gov.uk")
  val email: String?,
)
