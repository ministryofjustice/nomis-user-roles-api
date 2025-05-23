package uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DPS_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.EmailAddress
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
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
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.GroupCaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.time.LocalDate

class GeneralUserBuilder(
  repository: UserPersonDetailRepository,
  private val caseloadRepository: CaseloadRepository,
  private val groupCaseloadRepository: GroupCaseloadRepository,
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

  private fun generalUsersOf(prisonCodes: List<String>): MutableList<UserGroupMember> = prisonCodes.map { caseload ->
    groupCaseloadRepository.findAllById_Caseload(caseload).map {
      UserGroupMember(
        UserGroupMemberPk(it.userGroup.id, this.userPersonDetail.username),
        active = true,
        startDate = LocalDate.now().minusDays(1),
        userGroup = it.userGroup,
        user = this.userPersonDetail,
      )
    }
  }.flatten().toMutableList()

  override fun build(): GeneralUserBuilder {
    userPersonDetail =
      userPersonDetail.copy(
        activeAndInactiveMemberOfUserGroups = generalUsersOf(prisonCodes),
        type = UsageType.GENERAL,
        activeCaseLoad = prisonCodes.firstOrNull()?.let { caseloadRepository.findByIdOrNull(it) },
        caseloads = (prisonCodes + DPS_CASELOAD).map {
          UserCaseload(
            UserCaseloadPk(
              caseloadId = it,
              username = userPersonDetail.username,
            ),
            startDate = LocalDate.now().minusDays(1),
            caseload = caseloadRepository.findByIdOrNull(it)!!,
            user = userPersonDetail,
            roles = mutableListOf(),
          ).let { userCaseload ->
            userCaseload.copy(
              roles = asRoles(
                userCaseload,
                if (it == DPS_CASELOAD) dpsRoles else nomsRoles,
              ),
            )
          }
        }.toMutableList(),
      )
    return this
  }
}

class LocalAdministratorBuilder(
  repository: UserPersonDetailRepository,
  roleRepository: RoleRepository,
  private val caseloadRepository: CaseloadRepository,
  private val groupCaseloadRepository: GroupCaseloadRepository,
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

  private fun adminUsersOf(prisonCodes: List<String>): MutableList<UserGroupAdministrator> = prisonCodes.map { caseload ->
    groupCaseloadRepository.findAllById_Caseload(caseload).map {
      UserGroupAdministrator(
        UserGroupAdministratorPk(it.userGroup.id, this.userPersonDetail.username),
        active = this.userPersonDetail.isActive(),
        userGroup = it.userGroup,
        user = this.userPersonDetail,
      )
    }
  }.flatten().toMutableList()

  override fun build(): LocalAdministratorBuilder {
    userPersonDetail =
      userPersonDetail.copy(
        activeAndInactiveAdministratorOfUserGroups = adminUsersOf(prisonCodes),
        type = UsageType.ADMIN,
        activeCaseLoad = prisonCodes.firstOrNull()?.let { caseloadRepository.findByIdOrNull(it) },
      )
    return this
  }
}

@Component
class DataBuilder(
  private val repository: UserPersonDetailRepository,
  private val groupCaseloadRepository: GroupCaseloadRepository,
  private val caseloadRepository: CaseloadRepository,
  private val roleRepository: RoleRepository,
) {
  fun generalUser() = generalUserEntityCreator(
    repository = repository,
    groupCaseloadRepository = groupCaseloadRepository,
    caseloadRepository = caseloadRepository,
    roleRepository = roleRepository,
  )

  fun localAdministrator() = localAdministratorEntityCreator(
    repository = repository,
    groupCaseloadRepository = groupCaseloadRepository,
    caseloadRepository = caseloadRepository,
    roleRepository = roleRepository,
  )

  fun deleteAllUsers() {
    repository.deleteAll()
    repository.flush()
  }
}

fun generalUserEntityCreator(
  repository: UserPersonDetailRepository,
  groupCaseloadRepository: GroupCaseloadRepository,
  caseloadRepository: CaseloadRepository,
  roleRepository: RoleRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): GeneralUserBuilder = GeneralUserBuilder(
  repository = repository,
  groupCaseloadRepository = groupCaseloadRepository,
  caseloadRepository = caseloadRepository,
  roleRepository = roleRepository,
  userPersonDetail = userPersonDetail,
  prisonCodes = prisonCodes,
)

fun localAdministratorEntityCreator(
  repository: UserPersonDetailRepository,
  groupCaseloadRepository: GroupCaseloadRepository,
  caseloadRepository: CaseloadRepository,
  roleRepository: RoleRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): LocalAdministratorBuilder = LocalAdministratorBuilder(
  repository = repository,
  roleRepository = roleRepository,
  groupCaseloadRepository = groupCaseloadRepository,
  caseloadRepository = caseloadRepository,
  userPersonDetail = userPersonDetail,
  prisonCodes = prisonCodes,
)

fun defaultPerson(): UserPersonDetail = UserPersonDetail(
  username = "tony",
  staff = Staff(firstName = "John", lastName = "Smith", status = "ACTIVE"),
  type = UsageType.GENERAL,
  accountDetail = AccountDetail(accountStatus = AccountStatus.OPEN.desc, profile = AccountProfile.TAG_GENERAL.name),
)

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

  fun atPrisons(vararg prisonCodes: String): UserBuilder<T> = atPrisons(prisonCodes.toList())
  fun atPrison(prisonCode: String): UserBuilder<T> = atPrisons(prisonCode)

  fun firstName(firstName: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy(firstName = firstName))
    return this
  }

  fun lastName(lastName: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy(lastName = lastName))
    return this
  }

  fun email(email: String): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy())
    this.userPersonDetail.staff.setEmail(email)
    return this
  }

  fun addEmail(email: String): UserBuilder<T> {
    this.userPersonDetail.staff.emails.add(EmailAddress(emailCaseSensitive = email, staff = userPersonDetail.staff))
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

  internal fun asRoles(userCaseload: UserCaseload, roleCodes: List<String>): MutableList<UserCaseloadRole> = roleCodes.map { userCaseloadRole(userCaseload, it) }.toMutableList()

  private fun userCaseloadRole(userCaseload: UserCaseload, roleCode: String): UserCaseloadRole {
    val role = roleRepository.findByCode(roleCode).orElseThrow()
    return UserCaseloadRole(
      UserCaseloadRoleIdentity(role.id, this.userPersonDetail.username, userCaseload.caseload.id),
      role = role,
      userCaseload = userCaseload,
    )
  }

  fun inactive(): UserBuilder<T> = status(status = AccountStatus.LOCKED)

  fun status(status: AccountStatus): UserBuilder<T> {
    this.userPersonDetail =
      userPersonDetail.copy(accountDetail = userPersonDetail.accountDetail?.copy(accountStatus = status.desc))
    return this
  }
}
