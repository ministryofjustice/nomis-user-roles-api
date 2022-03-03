package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User & Role Information")
data class UserRoleDetail(
  @Schema(description = "Username", example = "TESTUSER1", required = true) val username: String,
  @Schema(description = "Indicates that the user is active", example = "true", required = true) val active: Boolean,
  @Schema(description = "Type of user account", example = "GENERAL", required = true) val accountType: UsageType = UsageType.GENERAL,
  @Schema(description = "Active Caseload of the user", example = "BXI", required = false) val activeCaseload: PrisonCaseload? = null,
  @Schema(description = "DPS Roles assigned to this user", required = false) val dpsRoles: List<RoleDetail> = listOf(),
  @Schema(description = "NOMIS Roles assigned to this user per caseload", required = false) val nomisRoles: List<CaseloadRoleDetail>? = null
)
