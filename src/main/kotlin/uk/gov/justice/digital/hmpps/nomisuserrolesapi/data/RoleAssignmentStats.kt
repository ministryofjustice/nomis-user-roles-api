package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema


@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role reassignment statistics")
data class RoleAssignmentStats (
  @Schema(required = true, description = "Caseload")
  private val caseload: String,

  @Schema(required = true, description = "Number of matched users")
  val numMatchedUsers : Int = 0,

  @Schema(required = true, description = "Number of role assignments succeeded")
  var numAssignRoleSucceeded: Long = 0,

  @Schema(required = true, description = "Number of role assignments failed")
  var numAssignRoleFailed: Long = 0,

  @Schema(required = true, description = "Number of role unassignments succeeded")
  var numUnassignRoleSucceeded: Long = 0,

  @Schema(required = true, description = "Number of role unassignments ignored - When role does not exist")
  var numUnassignRoleIgnored: Long = 0,

  @Schema(required = true, description = "Number of role unassignments failed")
  var numUnassignRoleFailed: Long = 0,
) {

  fun toMap(): Map<String, String> = mapOf(
    "caseload" to caseload,
    "numMatchedUsers" to numMatchedUsers.toString(),
    "numAssignRoleSucceeded" to numAssignRoleSucceeded.toString(),
    "numAssignRoleFailed" to numAssignRoleFailed.toString(),
    "numUnassignRoleSucceeded" to numUnassignRoleSucceeded.toString(),
    "numUnassignRoleIgnored" to numUnassignRoleIgnored.toString(),
    "numUnassignRoleFailed" to numUnassignRoleFailed.toString(),
  )

}

