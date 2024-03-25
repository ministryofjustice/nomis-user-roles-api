package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus

@Schema(description = "Summary User Information with Email Address")
data class GroupAdminSummaryWithEmail(
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
  @Schema(description = "Account status", example = "OPEN")
  val status: AccountStatus?,
  @Schema(description = "Indicates that an account is locked.", example = "false")
  val locked: Boolean = false,
  @Schema(description = "Indicates that an account is expired", example = "false")
  val expired: Boolean = false,
  @Schema(description = "Caseload that is currently active, typically the prison the user is currently working at")
  val activeCaseload: PrisonCaseload?,
  @Schema(description = "The count of DPS roles allocated to this staff member", example = "12")
  val dpsRoleCount: Int,
  @Schema(
    description = "Primary email address of user - normally justice.gov.uk one if available otherwise first one in list",
    example = "joe.bloggs@justice.gov.uk",
  )
  val email: String?,
  @Schema(
    description = "Groups that the user is administrator of",
    example = "[{id: BXI, description: }]",
  )
  val groups: List<UserGroupDetail>,
  @Schema(description = "Staff status", example = "ACTIVE")
  val staffStatus: String?,
)
