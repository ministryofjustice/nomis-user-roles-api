package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Roles in caseload information")
data class CaseloadRoleDetail(
  @Schema(description = "Caseload for the listed roles", required = true) val caseload: PrisonCaseload,
  @Schema(description = "NOMIS Roles assigned to this user", required = false) val roles: List<RoleDetail> = listOf(),
)
