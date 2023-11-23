package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role Reassignment Specification")
data class RoleAssignmentsSpecification(
  @Schema(required = true, description = "The caseloads to search for users having roles matching 'nomisRolesToMatch'.")
  @NotEmpty(message = "Expected at least one 'caseload'")
  val caseloads: List<String>,

  @Schema(
    required = true,
    description = "users within the caseloads will be selected if they have at least one role matching the codes in rolesToMatch.",
  )
  @NotEmpty(message = "Expected at least one 'nomisRolesToMatch'")
  val nomisRolesToMatch: List<String>,

  @Schema(description = "Users with the named caseloads, having roles matching nomisRolesToMatch will be assigned these DPS roles")
  val dpsRolesToAssign: List<String> = listOf(),

  @Schema(description = "For each caseload in caseloads; find the users having at least one role matching 'nomisRolesToMatch'. For each matched user at the current caseload remove each of the roles in 'nomisRolesToRemove' at that caseload.")
  val nomisRolesToRemove: List<String> = listOf(),
)
