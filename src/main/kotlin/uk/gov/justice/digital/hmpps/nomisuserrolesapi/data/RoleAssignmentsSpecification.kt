package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull


@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role Reassignment Specification")
data class RoleAssignmentsSpecification (
  @Schema(required = true, description = "The caseloads to search for users having roles matching 'rolesToMatch'.")
  @NotEmpty(message = "Expected at least one 'caseload'")
  val caseloads: List<String>,

  @Schema(
    required = true,
    description = "users within the caseloads will be selected if they have at least one role matching the codes in rolesToMatch.",
  )
  @NotEmpty(message = "Expected at least one 'rolesToMatch'")
  val rolesToMatch: List<String>,

  @Schema(description = "Users with the named caseloads, having roles matching rolesToMatch will be assigned these roles in the 'NWEB' caseload.")
  @NotNull
  val rolesToAssign: List<String> = listOf(),

  @Schema(description = "For each caseload in caseloads; find the users having at least one role matching 'rolesToMatch'. For each matched user at the current caseload remove each of the roles in 'rolesToRemove' at that caseload.")
  @NotNull
  val rolesToRemove: List<String> = listOf(),
)

