package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UpdateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.RoleType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.getUsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toRoleDetail
import java.util.function.Supplier
import javax.transaction.Transactional

@Service
@Transactional
class RoleService(
  private val roleRepository: RoleRepository
) {
  fun createRole(createRoleRequest: CreateRoleRequest): RoleDetail {
    roleRepository.findByCode(createRoleRequest.code).ifPresent { throw UserRoleAlreadyExistsException("Role with code ${it.code} already exists") }

    return roleRepository.save(
      Role(
        code = createRoleRequest.code.uppercase(),
        name = createRoleRequest.name,
        sequence = createRoleRequest.sequence,
        roleFunction = getUsageType(createRoleRequest.adminRoleOnly),
        type = createRoleRequest.type,
        parent = createRoleRequest.parentRoleCode?.let {
          roleRepository.findByCode(createRoleRequest.parentRoleCode)
            .orElseThrow(UserRoleNotFoundException("Parent role with code ${createRoleRequest.parentRoleCode} not found"))
        }
      )
    ).toRoleDetail()
  }

  fun getAllRoles(): List<RoleDetail> {
    return roleRepository.findAll().map {
      it.toRoleDetail()
    }
  }

  fun getAllDPSRoles(): List<RoleDetail> {
    return roleRepository.findAllByType(RoleType.APP).map {
      it.toRoleDetail()
    }
  }

  fun findByCode(roleCode: String): RoleDetail =
    roleRepository.findByCode(roleCode).map { it.toRoleDetail() }.orElseThrow(UserRoleNotFoundException("Role with code $roleCode not found"))

  fun deleteRole(roleCode: String) {
    val roleToDelete = roleRepository.findByCode(roleCode).orElseThrow(UserRoleNotFoundException("Role with code $roleCode not found"))
    roleRepository.deleteById(roleToDelete.id)
  }

  fun updateRole(roleCode: String, updateRoleRequest: UpdateRoleRequest): RoleDetail {
    val role = roleRepository.findByCode(roleCode)
      .orElseThrow { UserRoleNotFoundException("Role with code $roleCode not found") }

    with(role) {
      name = updateRoleRequest.name
      sequence = updateRoleRequest.sequence
      parent = updateRoleRequest.parentRoleCode?.let {
        roleRepository.findByCode(updateRoleRequest.parentRoleCode)
          .orElseThrow(UserRoleNotFoundException("Parent role with code ${updateRoleRequest.parentRoleCode} not found"))
      }

      roleFunction = getUsageType(updateRoleRequest.adminRoleOnly)
      type = updateRoleRequest.type
    }
    return role.toRoleDetail()
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
