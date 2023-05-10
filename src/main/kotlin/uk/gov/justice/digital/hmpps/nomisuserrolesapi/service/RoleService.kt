package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UpdateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.RoleType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.getUsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toRoleDetail
import java.util.function.Supplier

@Service
@Transactional
class RoleService(
  private val roleRepository: RoleRepository,
  private val telemetryClient: TelemetryClient,
  private val authenticationFacade: AuthenticationFacade,
) {
  fun createRole(createRoleRequest: CreateRoleRequest): RoleDetail {
    roleRepository.findByCode(createRoleRequest.code)
      .ifPresent { throw UserRoleAlreadyExistsException("Role with code ${it.code} already exists") }

    val roleDetail = roleRepository.save(
      Role(
        code = createRoleRequest.code.uppercase(),
        name = createRoleRequest.name,
        sequence = createRoleRequest.sequence,
        roleFunction = getUsageType(createRoleRequest.adminRoleOnly),
        type = createRoleRequest.type,
        parent = createRoleRequest.parentRoleCode?.let {
          roleRepository.findByCode(createRoleRequest.parentRoleCode)
            .orElseThrow(UserRoleNotFoundException("Parent role with code ${createRoleRequest.parentRoleCode} not found"))
        },
      ),
    ).toRoleDetail()

    telemetryClient.trackEvent(
      "NURA-role-created",
      mapOf(
        "role" to roleDetail.code,
        "name" to roleDetail.name,
        "admin-role-only" to roleDetail.adminRoleOnly.toString(),
        "type" to roleDetail.type?.name,
        "admin" to authenticationFacade.currentUsername,
      ),
      null,
    )
    return roleDetail
  }

  fun getAllRoles(): List<RoleDetail> {
    return roleRepository.findAll().map {
      it.toRoleDetail()
    }
  }

  fun getAllDPSRoles(adminRoles: Boolean): List<RoleDetail> {
    return roleRepository.findAllByTypeAndRoleFunctionIn(
      RoleType.APP,
      UsageType.values().toList().filter { adminRoles || it !== UsageType.ADMIN },
    ).map {
      it.toRoleDetail()
    }
  }

  fun findByCode(roleCode: String): RoleDetail =
    roleRepository.findByCode(roleCode).map { it.toRoleDetail() }
      .orElseThrow(UserRoleNotFoundException("Role with code $roleCode not found"))

  fun deleteRole(roleCode: String) {
    val roleToDelete =
      roleRepository.findByCode(roleCode).orElseThrow(UserRoleNotFoundException("Role with code $roleCode not found"))
    roleRepository.deleteById(roleToDelete.id)

    telemetryClient.trackEvent(
      "NURA-role-deleted",
      mapOf(
        "role" to roleToDelete.code,
        "name" to roleToDelete.name,
        "admin" to authenticationFacade.currentUsername,
      ),
      null,
    )
  }

  fun updateRole(roleCode: String, updateRoleRequest: UpdateRoleRequest): RoleDetail {
    val role = roleRepository.findByCode(roleCode)
      .orElseThrow { UserRoleNotFoundException("Role with code $roleCode not found") }

    with(role) {
      name = updateRoleRequest.name ?: role.name
      sequence = updateRoleRequest.sequence ?: role.sequence
      parent = updateRoleRequest.parentRoleCode?.let {
        roleRepository.findByCode(updateRoleRequest.parentRoleCode)
          .orElseThrow(UserRoleNotFoundException("Parent role with code ${updateRoleRequest.parentRoleCode} not found"))
      }

      roleFunction = getUsageType(updateRoleRequest.adminRoleOnly ?: (role.roleFunction == UsageType.ADMIN))
      type = updateRoleRequest.type ?: role.type
    }

    val roleDetail = role.toRoleDetail()
    telemetryClient.trackEvent(
      "NURA-role-updated",
      mapOf(
        "role" to roleDetail.code,
        "name" to roleDetail.name,
        "admin-role-only" to roleDetail.adminRoleOnly.toString(),
        "type" to roleDetail.type?.name,
        "admin" to authenticationFacade.currentUsername,
      ),
      null,
    )
    return roleDetail
  }
}

class UserRoleNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<UserRoleNotFoundException> {
  override fun get(): UserRoleNotFoundException {
    return UserRoleNotFoundException(message)
  }
}

class UserRoleAlreadyExistsException(message: String?) :
  RuntimeException(message),
  Supplier<UserRoleAlreadyExistsException> {
  override fun get(): UserRoleAlreadyExistsException {
    return UserRoleAlreadyExistsException(message)
  }
}
