package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleAssignmentStats
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleAssignmentsSpecification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.*
import java.util.*

@Service
class RoleAssignmentsService(
  private val userCaseloadRoleRepository: UserCaseloadRoleRepository,
  private val roleRepository: RoleRepository,
  private val telemetryClient: TelemetryClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * Look up users by caseload (prison) and membership of specification.rolesToMatch.
   * for each matched user:
   * Assign to the user the roles identified in specification.rolesToAssign.
   * Remove from the user (at the caseload) the roles identified by specification.rolesToRemove.
   */

  fun updateRoleAssignments(specification: RoleAssignmentsSpecification): List<RoleAssignmentStats> {
    log.info("Updating role assignments: {}", specification)
    return specification.caseloads.map { caseload ->
      performAssignment(specification, caseload)
    }
  }

  private fun performAssignment(specification: RoleAssignmentsSpecification, caseload: String): RoleAssignmentStats {
    val usernamesForCaseload = findUsernamesMatchingRolesAtCaseload(specification.rolesToMatch, caseload)
    log.info("Found {} users for the {} caseload: {}.", usernamesForCaseload.size, caseload, usernamesForCaseload)

    val results = RoleAssignmentStats(
      numMatchedUsers = usernamesForCaseload.size,
      caseload = caseload,
    )

    usernamesForCaseload.forEach { caseload ->

      val assignmentSuccess = assignRolesToUser(results, caseload.id.username, specification.rolesToAssign)
      if (assignmentSuccess) {
        removeRolesFromUserAtCaseload(results, caseload, username, specification.rolesToRemove)
      }
    }
    telemetryClient.trackEvent("UpdateRoleAssignment", results.toMap(), null)
    return results
  }

  private fun findUsernamesMatchingRolesAtCaseload(
    rolesToMatch: List<String>,
    caseload: String,
  ) = rolesToMatch.flatMap { roleCode ->
      userCaseloadRoleRepository.findAllById_caseloadAndRole_code(caseload, roleCode)
    }

  private fun assignRolesToUser(stats: RoleAssignmentStats, username: String, rolesToAssign: List<String>): Boolean {
    return false
  }

  private fun assignRole(stats: RoleAssignmentStats, username: String, roleCodeToAssign: String): Boolean {
    return try {
      roleService.assignRoleToApiCaseload(username, roleCodeToAssign)
      stats.incrementAssignmentSuccess()
      true
    } catch (e: Exception) {
      val message = String.format(
        "Failure while assigning roles %1\$s to user %2\$s. No roles will be removed from this user. Continuing with next user.",
        roleCodeToAssign,
        username,
      )
      log.warn(message, e)
      stats.incrementAssignmentFailure()
      false
    }
  }
}