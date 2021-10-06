package uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRoleIdentity
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministrator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministratorPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMemberPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserGroupRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.time.LocalDate

class GeneralUserBuilder(
  repository: UserPersonDetailRepository,
  private val userGroupRepository: UserGroupRepository,
  private val caseloadRepository: CaseloadRepository,
  roleRepository: RoleRepository,
  userPersonDetail: UserPersonDetail,
  prisonCodes: List<String>,
) : UserBuilder<GeneralUserBuilder>(
  repository = repository,
  roleRepository = roleRepository,
  userPersonDetail = userPersonDetail,
  prisonCodes = prisonCodes,
  dpsRoles = listOf(),
  nomsRoles = listOf(),
) {

  private fun generalUsersOf(prisonCodes: List<String>): List<UserGroupMember> {
    return prisonCodes.map {
      UserGroupMember(
        UserGroupMemberPk(it, this.userPersonDetail.username),
        active = true,
        startDate = LocalDate.now().minusDays(1),
        userGroup = userGroupRepository.findByIdOrNull(it)!!,
        user = this.userPersonDetail
      )
    }
  }

  override fun build(): GeneralUserBuilder {
    val dpsCaseloadId = "NWEB"
    userPersonDetail =
      userPersonDetail.copy(
        activeAndInactiveMemberOfUserGroups = generalUsersOf(prisonCodes),
        type = "GENERAL",
        activeCaseLoad = prisonCodes.firstOrNull()?.let { caseloadRepository.findByIdOrNull(it) },
        caseloads = (prisonCodes + dpsCaseloadId).map {
          val userCaseload = UserCaseload(
            UserCaseloadPk(
              caseloadId = it,
              username = userPersonDetail.username
            ),
            startDate = LocalDate.now().minusDays(1),
            caseload = caseloadRepository.findByIdOrNull(it)!!,
            user = userPersonDetail,
            roles = listOf(),
          )
          // add after userCaseload is created due to back ref
          userCaseload.copy(roles = asRoles(userCaseload, if (it == dpsCaseloadId) dpsRoles else nomsRoles))
        }.toMutableList()
      )
    return this
  }
}

class LocalAdministratorBuilder(
  repository: UserPersonDetailRepository,
  roleRepository: RoleRepository,
  private val userGroupRepository: UserGroupRepository,
  private val caseloadRepository: CaseloadRepository,
  userPersonDetail: UserPersonDetail,
  prisonCodes: List<String>,
) : UserBuilder<LocalAdministratorBuilder>(
  repository = repository,
  roleRepository = roleRepository,
  userPersonDetail = userPersonDetail,
  prisonCodes = prisonCodes,
  dpsRoles = listOf(),
  nomsRoles = listOf(),
) {

  private fun adminUsersOf(prisonCodes: List<String>): List<UserGroupAdministrator> {
    return prisonCodes.map {
      UserGroupAdministrator(
        UserGroupAdministratorPk(it, this.userPersonDetail.username),
        active = true,
        userGroup = userGroupRepository.findByIdOrNull(it)!!,
        user = this.userPersonDetail,
      )
    }
  }

  override fun build(): LocalAdministratorBuilder {
    userPersonDetail =
      userPersonDetail.copy(
        activeAndInactiveAdministratorOfUserGroups = adminUsersOf(prisonCodes),
        type = "ADMIN",
        activeCaseLoad = prisonCodes.firstOrNull()?.let { caseloadRepository.findByIdOrNull(it) },
      )
    return this
  }
}

@Component
class DataBuilder(
  private val repository: UserPersonDetailRepository,
  private val userGroupRepository: UserGroupRepository,
  private val caseloadRepository: CaseloadRepository,
  private val roleRepository: RoleRepository,
) {
  fun generalUser() = generalUserEntityCreator(
    repository = repository,
    userGroupRepository = userGroupRepository,
    caseloadRepository = caseloadRepository,
    roleRepository = roleRepository
  )

  fun localAdministrator() = localAdministratorEntityCreator(
    repository = repository,
    userGroupRepository = userGroupRepository,
    caseloadRepository = caseloadRepository,
    roleRepository = roleRepository
  )

  fun deleteAllUsers() {
    repository.deleteAll()
    repository.flush()
  }
}

fun generalUserEntityCreator(
  repository: UserPersonDetailRepository,
  userGroupRepository: UserGroupRepository,
  caseloadRepository: CaseloadRepository,
  roleRepository: RoleRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): GeneralUserBuilder {
  return GeneralUserBuilder(
    repository = repository,
    userGroupRepository = userGroupRepository,
    caseloadRepository = caseloadRepository,
    roleRepository = roleRepository,
    userPersonDetail = userPersonDetail,
    prisonCodes = prisonCodes
  )
}

fun localAdministratorEntityCreator(
  repository: UserPersonDetailRepository,
  userGroupRepository: UserGroupRepository,
  caseloadRepository: CaseloadRepository,
  roleRepository: RoleRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): LocalAdministratorBuilder {
  return LocalAdministratorBuilder(
    repository = repository,
    roleRepository = roleRepository,
    userGroupRepository = userGroupRepository,
    caseloadRepository = caseloadRepository,
    userPersonDetail = userPersonDetail,
    prisonCodes = prisonCodes
  )
}

fun defaultPerson(): UserPersonDetail {
  return UserPersonDetail(
    username = "tony",
    staff = Staff(firstName = "John", lastName = "Smith", status = "ACTIVE"),
    type = "GENERAL"
  )
}

abstract class UserBuilder<T>(
  private val repository: UserPersonDetailRepository,
  private val roleRepository: RoleRepository,
  internal var userPersonDetail: UserPersonDetail,
  internal var prisonCodes: List<String>,
  internal var nomsRoles: List<String>,
  internal var dpsRoles: List<String>,
) {

  fun save(): UserPersonDetail {
    repository.saveAndFlush(userPersonDetail)
    return userPersonDetail
  }

  abstract fun build(): UserBuilder<T>

  fun buildAndSave(): UserPersonDetail {
    build()
    repository.saveAndFlush(userPersonDetail)
    return userPersonDetail
  }

  fun transform(transformer: (UserPersonDetail) -> UserPersonDetail): UserBuilder<T> {
    userPersonDetail = transformer(userPersonDetail)
    return this
  }

  fun username(username: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(username = username)
    return this
  }

  fun atPrisons(prisonCodes: List<String>): UserBuilder<T> {
    this.prisonCodes = prisonCodes
    return this
  }

  fun atPrison(prisonCode: String): UserBuilder<T> {
    this.prisonCodes = listOf(prisonCode)
    return this
  }

  fun firstName(firstName: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy(firstName = firstName))
    return this
  }

  fun lastName(lastName: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy(lastName = lastName))
    return this
  }

  fun dpsRoles(roles: List<String>): UserBuilder<T> {
    this.dpsRoles = roles
    return this
  }

  fun nomisRoles(roles: List<String>): UserBuilder<T> {
    this.nomsRoles = roles
    return this
  }

  internal fun asRoles(userCaseload: UserCaseload, roleCodes: List<String>): List<UserCaseloadRole> =
    roleCodes.map { userCaseloadRole(userCaseload, it) }

  private fun userCaseloadRole(userCaseload: UserCaseload, roleCode: String): UserCaseloadRole {
    val role = roleRepository.findByCode(roleCode)!!
    return UserCaseloadRole(
      UserCaseloadRoleIdentity(role.id, this.userPersonDetail.username, userCaseload.caseload.id),
      role = role,
      userCaseload = userCaseload,
    )
  }

  fun inactive(): UserBuilder<T> = status(status = "INACT")

  fun status(status: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy(status = status))
    return this
  }
}
