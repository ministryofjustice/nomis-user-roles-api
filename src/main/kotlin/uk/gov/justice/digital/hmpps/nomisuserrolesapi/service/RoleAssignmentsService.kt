package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleAssignmentStats
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleAssignmentsSpecification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DPS_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserCaseloadRoleRepository

@Service
@Transactional
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

    val nomisRolesToMatch = specification.nomisRolesToMatch.map { roleCode ->
      roleRepository.findByCode(roleCode).orElseThrow(UserRoleNotFoundException("NOMIS role with code $roleCode not found"))
    }

    val dpsRolesToAssign = specification.dpsRolesToAssign.map { roleCode ->
      roleRepository.findByCode(roleCode).orElseThrow(UserRoleNotFoundException("DPS role with code $roleCode not found"))
    }

    val nomisRolesToRemove = specification.nomisRolesToRemove.map { roleCode ->
      roleRepository.findByCode(roleCode).orElseThrow(UserRoleNotFoundException("NOMIS role with code $roleCode not found"))
    }

    return specification.caseloads.map { caseload ->
      performAssignment(caseload, nomisRolesToMatch, dpsRolesToAssign, nomisRolesToRemove)
    }
  }

  private fun performAssignment(caseload: String, nomisRolesToMatch: List<Role>, dpsRolesToAssign: List<Role>, nomisRolesToRemove: List<Role>): RoleAssignmentStats {
    val usersToUpdate = nomisRolesToMatch.flatMap { role ->
      userCaseloadRoleRepository.findAllByIdCaseloadAndIdRoleId(caseload, role.id).map {
        it.userCaseload.user
      }
    }.distinct()

    log.info("Found {} users for the {} caseload: {}.", usersToUpdate.size, caseload, usersToUpdate.map { it.username })

    val results = RoleAssignmentStats(
      numMatchedUsers = usersToUpdate.size,
      caseload = caseload,
    )

    usersToUpdate.forEach { user ->
      dpsRolesToAssign.forEach { roleToAdd ->
        try {
          user.addRole(roleToAdd, DPS_CASELOAD)
          results.numAssignRoleSucceeded++
        } catch (e: Exception) {
          log.warn("Failed to add DPS role {} to user {}: {}", roleToAdd.code, user.username, e.message)
          results.numAssignRoleFailed++
        }
        nomisRolesToRemove.forEach { roleToRemove ->
          try {
            user.removeRole(roleToRemove.code, caseload)
            results.numUnassignRoleSucceeded++
          } catch (e: Exception) {
            log.warn("Failed to remove NOMIS role {} from user {} at caseload {}: {}", roleToRemove.code, user.username, caseload, e.message)
            results.numUnassignRoleFailed++
          }
        }
      }
    }
    log.info("Role assignment results: {}", results)

    telemetryClient.trackEvent("UpdateRoleAssignment", results.toMap(), null)
    return results
  }
}
