package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import UserSpecification
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.data.domain.Sort.by
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserCaseloadDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummaryWithEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DPS_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.getUsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.AccountDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserAndEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserAndEmailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPasswordRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.changePasswordWithValidation
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.toUserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.toUserSummaryWithEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.mapUserSummarySortProperties
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toStaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toUserCaseloadDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toUserRoleDetail
import java.time.LocalDateTime
import java.util.EnumSet
import java.util.function.Supplier
import java.util.stream.Collectors

@Service
@Transactional
class UserService(
  private val userPersonDetailRepository: UserPersonDetailRepository,
  private val userAndEmailRepository: UserAndEmailRepository,
  private val caseloadRepository: CaseloadRepository,
  private val accountDetailRepository: AccountDetailRepository,
  private val staffRepository: StaffRepository,
  private val roleRepository: RoleRepository,
  private val telemetryClient: TelemetryClient,
  private val authenticationFacade: AuthenticationFacade,
  private val passwordEncoder: PasswordEncoder,
  private val userPasswordRepository: UserPasswordRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun findByUsername(username: String): UserDetail =
    userPersonDetailRepository.findById(username)
      .map(this::toUserDetail)
      .orElseThrow(UserNotFoundException("User $username not found"))

  @Transactional(readOnly = true)
  fun findUsersByFirstAndLastNames(firstName: String, lastName: String): List<UserSummaryWithEmail> =
    userPersonDetailRepository.findAllByStaff_FirstNameIgnoreCaseAndStaff_LastNameIgnoreCase(firstName, lastName)
      .map { it.toUserSummaryWithEmail() }

  @Transactional(readOnly = true)
  fun findAllByEmailAddress(emailAddress: String): List<UserDetail> =
    userPersonDetailRepository.findByStaff_EmailsEmail(emailAddress)
      .map(this::toUserDetail)

  @Transactional(readOnly = true)
  fun findAllByEmailAddressAndUsernames(emailAddress: String, usernames: List<String>?): List<UserDetail> {
    val usersByEmail = userPersonDetailRepository.findByStaff_EmailsEmail(emailAddress)
    val users = if (usernames.isNullOrEmpty()) usersByEmail
    else usersByEmail.union(userPersonDetailRepository.findAllById(usernames))
    return users.map(this::toUserDetail)
  }

  @Transactional(readOnly = true)
  fun findUsersAndEmails(): List<UserAndEmail> {
    log.info("Retrieving users and emails from database...")
    val usersAndEmails = userAndEmailRepository.findUsersAndEmails()
    log.info("Done retrieving users and emails from database...")
    return usersAndEmails
  }

  @Transactional(readOnly = true)
  fun findUsersByFilter(pageRequest: Pageable, filter: UserFilter): Page<UserSummaryWithEmail> =
    userPersonDetailRepository.findAll(UserSpecification(filter), pageRequest.withSort(::mapUserSummarySortProperties))
      .map {
        it.toUserSummaryWithEmail(accountDetailRepository.findById(it.username).orElse(AccountDetail(username = it.username, accountStatus = AccountStatus.OPEN.desc)).status)
      }

  fun createGeneralUser(createUserRequest: CreateGeneralUserRequest): UserSummary {

    checkIfAccountAlreadyExists(createUserRequest.username)

    val staffAccount =
      createStaffRecord(createUserRequest.firstName, createUserRequest.lastName, createUserRequest.email)

    if (staffAccount.generalAccount() != null) {
      throw UserAlreadyExistsException("General user already exists for this staff member")
    }

    val userPersonDetail =
      createUserAccount(staffAccount, createUserRequest.username, createUserRequest.defaultCaseloadId)

    createSchemaUser(createUserRequest.username, AccountProfile.TAG_GENERAL)

    telemetryClient.trackEvent(
      "NURA-general-user-created",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
    return userPersonDetail.toUserSummary()
  }

  fun linkGeneralAccount(linkedUsername: String, linkedUserRequest: CreateLinkedGeneralUserRequest): UserSummary {
    checkIfAccountAlreadyExists(linkedUserRequest.username)

    val staffAccount = userPersonDetailRepository.findById(linkedUsername).map { it.staff }
      .orElseThrow { UserNotFoundException("Linked User Account $linkedUsername not found") }

    if (staffAccount.generalAccount() != null) {
      throw UserAlreadyExistsException("General user already exists for this staff member")
    }

    val userPersonDetail =
      createUserAccount(staffAccount, linkedUserRequest.username, linkedUserRequest.defaultCaseloadId)
    createSchemaUser(linkedUserRequest.username, AccountProfile.TAG_GENERAL)

    telemetryClient.trackEvent(
      "NURA-link-general-user",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "linked-to" to linkedUsername,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
    return userPersonDetail.toUserSummary()
  }

  fun createAdminUser(createUserRequest: CreateAdminUserRequest): UserSummary {

    checkIfAccountAlreadyExists(createUserRequest.username)

    val staffAccount =
      createStaffRecord(createUserRequest.firstName, createUserRequest.lastName, createUserRequest.email)

    if (staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail =
      createUserAccount(staffAccount, createUserRequest.username, defaultCaseloadId = "CADM_I", admin = true)

    createSchemaUser(createUserRequest.username, AccountProfile.TAG_ADMIN)

    telemetryClient.trackEvent(
      "NURA-admin-user-created",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return userPersonDetail.toUserSummary()
  }

  fun linkAdminAccount(linkedUsername: String, linkedUserRequest: CreateLinkedAdminUserRequest): UserSummary {
    checkIfAccountAlreadyExists(linkedUserRequest.username)

    val staffAccount = userPersonDetailRepository.findById(linkedUsername).map { it.staff }
      .orElseThrow { UserNotFoundException("Linked User Account $linkedUsername not found") }

    if (staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail =
      createUserAccount(staffAccount, linkedUserRequest.username, defaultCaseloadId = "CADM_I", admin = true)
    createSchemaUser(linkedUserRequest.username, AccountProfile.TAG_ADMIN)

    telemetryClient.trackEvent(
      "NURA-link-admin-user",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "linked-to" to linkedUsername,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return userPersonDetail.toUserSummary()
  }

  fun createLocalAdminUser(createUserRequest: CreateGeneralUserRequest): UserSummary {

    checkIfAccountAlreadyExists(createUserRequest.username)

    val staffAccount =
      createStaffRecord(createUserRequest.firstName, createUserRequest.lastName, createUserRequest.email)

    if (staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail = createUserAccount(
      staffAccount,
      createUserRequest.username,
      defaultCaseloadId = createUserRequest.defaultCaseloadId,
      admin = true,
      laaAdmin = true
    )

    createSchemaUser(createUserRequest.username, AccountProfile.TAG_ADMIN)

    telemetryClient.trackEvent(
      "NURA-local-admin-user-created",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
    return userPersonDetail.toUserSummary()
  }

  fun linkLocalAdminAccount(linkedUsername: String, linkedUserRequest: CreateLinkedGeneralUserRequest): UserSummary {
    checkIfAccountAlreadyExists(linkedUserRequest.username)

    val staffAccount = userPersonDetailRepository.findById(linkedUsername).map { it.staff }
      .orElseThrow { UserNotFoundException("Linked User Account $linkedUsername not found") }

    if (staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail = createUserAccount(
      staffAccount,
      linkedUserRequest.username,
      defaultCaseloadId = linkedUserRequest.defaultCaseloadId,
      admin = true,
      laaAdmin = true
    )
    createSchemaUser(linkedUserRequest.username, AccountProfile.TAG_ADMIN)

    telemetryClient.trackEvent(
      "NURA-link-local-admin-user",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "linked-to" to linkedUsername,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return userPersonDetail.toUserSummary()
  }

  fun updateEmail(username: String, email: String): StaffDetail {
    val user = userPersonDetailRepository.findById(username)
      .orElseThrow(UserNotFoundException("User $username not found"))

    val oldEmail = user.staff.primaryEmail()?.email
    user.staff.setEmail(email)

    telemetryClient.trackEvent(
      "NURA-change-email",
      mapOf(
        "username" to username,
        "old-email" to oldEmail,
        "new-email" to email,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return user.staff.toStaffDetail()
  }

  fun updateStaffName(username: String, firstName: String, lastName: String): StaffDetail {
    val user = userPersonDetailRepository.findById(username)
      .orElseThrow(UserNotFoundException("User $username not found"))

    with(user.staff) {
      val oldFullName = this.fullName()

      this.firstName = firstName.uppercase()
      this.lastName = lastName.uppercase()

      telemetryClient.trackEvent(
        "NURA-change-staff-name",
        mapOf(
          "username" to username,
          "old-name" to oldFullName,
          "new-name" to this.fullName(),
          "admin" to authenticationFacade.currentUsername
        ),
        null
      )
    }

    return user.staff.toStaffDetail()
  }

  fun deleteUser(username: String) {
    val userPersonDetail = userPersonDetailRepository.findById(username)
      .orElseThrow(UserNotFoundException("User $username not found"))

    userPersonDetailRepository.delete(userPersonDetail)
    userPersonDetailRepository.dropUser(username)

    telemetryClient.trackEvent(
      "NURA-drop-user",
      mapOf(
        "username" to userPersonDetail.username,
        "type" to userPersonDetail.type.name,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
  }

  @Transactional(readOnly = true)
  fun findByStaffId(staffId: Long): StaffDetail {
    return staffRepository.findById(staffId)
      .map { s -> StaffDetail(s) }
      .orElseThrow(UserNotFoundException("Staff ID $staffId not found"))
  }

  fun lockUser(username: String) {
    userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    userPersonDetailRepository.lockUser(username)

    telemetryClient.trackEvent(
      "NURA-lock-user",
      mapOf(
        "username" to username,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
  }

  fun unlockUser(username: String) {
    userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    userPersonDetailRepository.unlockUser(username)

    telemetryClient.trackEvent(
      "NURA-unlock-user",
      mapOf(
        "username" to username,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
  }

  fun changePassword(username: String, password: String) {
    userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    changePasswordWithValidation(username, password, userPersonDetailRepository::changePassword)

    telemetryClient.trackEvent(
      "NURA-change-password",
      mapOf(
        "username" to username,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
  }

  fun setDefaultCaseload(username: String, defaultCaseloadId: String): UserCaseloadDetail {
    val user =
      userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    user.setDefaultCaseload(defaultCaseloadId)

    telemetryClient.trackEvent(
      "NURA-set-default-caseload",
      mapOf(
        "username" to username,
        "caseload" to defaultCaseloadId,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
    return user.toUserCaseloadDetail()
  }

  fun addCaseloadToUser(username: String, caseloadId: String): UserCaseloadDetail {
    val user =
      userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))

    val caseload = caseloadRepository.findById(caseloadId)
      .orElseThrow(CaseloadNotFoundException("Caseload $caseloadId not found"))

    user.addCaseload(caseload)

    telemetryClient.trackEvent(
      "NURA-add-caseload",
      mapOf(
        "username" to username,
        "caseload" to caseloadId,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return user.toUserCaseloadDetail()
  }

  @Transactional(readOnly = true)
  fun getCaseloads(username: String): UserCaseloadDetail {
    return userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
      .toUserCaseloadDetail(removeDpsCaseload = true)
  }

  fun removeCaseload(username: String, caseloadId: String): UserCaseloadDetail {
    val user =
      userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    user.removeCaseload(caseloadId)

    telemetryClient.trackEvent(
      "NURA-remove-caseload",
      mapOf(
        "username" to username,
        "caseload" to caseloadId,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return user.toUserCaseloadDetail()
  }

  fun addRolesToUser(username: String, roleCodes: List<String>, caseloadId: String = DPS_CASELOAD): UserRoleDetail {
    val user = setupCaseloadForUser(username, caseloadId)
    roleCodes.forEach { addRole(it, user, caseloadId) }
    return user.toUserRoleDetail()
  }

  fun addRoleToUser(username: String, roleCode: String, caseloadId: String = DPS_CASELOAD): UserRoleDetail {
    val user = setupCaseloadForUser(username, caseloadId)
    addRole(roleCode, user, caseloadId)
    return user.toUserRoleDetail()
  }

  fun removeRoleFromUser(username: String, roleCode: String, caseloadId: String = DPS_CASELOAD): UserRoleDetail {
    val user =
      userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    user.removeRole(roleCode, caseloadId)

    telemetryClient.trackEvent(
      "NURA-remove-role-from-user",
      mapOf(
        "username" to username,
        "role-code" to roleCode,
        "caseload" to caseloadId,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )

    return user.toUserRoleDetail()
  }

  fun removeRoleFromUsers(users: List<String>, roleCode: String): List<UserRoleDetail> =
    users.mapNotNull { userPersonDetailRepository.findByIdOrNull(it) }
      .map {
        it.also { user ->
          kotlin.runCatching { user.removeRole(roleCode, DPS_CASELOAD) }
            .onFailure { error ->
              log.warn("Unable to remove role $roleCode from ${user.username}", error)
            }
            .onSuccess {
              telemetryClient.trackEvent(
                "NURA-remove-role-from-user",
                mapOf(
                  "username" to user.username,
                  "role-code" to roleCode,
                  "caseload" to DPS_CASELOAD,
                  "admin" to authenticationFacade.currentUsername
                ),
                null
              )
            }
        }
          .toUserRoleDetail()
      }

  fun getUserRoles(username: String, includeNomisRoles: Boolean = false): UserRoleDetail {
    val user =
      userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    return user.toUserRoleDetail(includeNomisRoles)
  }

  private fun setupCaseloadForUser(
    username: String,
    caseloadId: String
  ): UserPersonDetail {
    val user =
      userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))

    // Special case: check to see if the DPS caseload is set up - if not set it up now
    if (caseloadId == DPS_CASELOAD) {
      user.findCaseloadById(DPS_CASELOAD) ?: user.addCaseload(
        caseloadRepository.findById(DPS_CASELOAD)
          .orElseThrow(CaseloadNotFoundException("Caseload $DPS_CASELOAD not found"))
      )
    }
    return user
  }

  private fun addRole(
    roleCode: String,
    user: UserPersonDetail,
    caseloadId: String
  ) {
    val role = roleRepository.findByCode(roleCode).orElseThrow { UserRoleNotFoundException("Role $roleCode not found") }
    user.addRole(role, caseloadId)

    telemetryClient.trackEvent(
      "NURA-add-role-to-user",
      mapOf(
        "username" to user.username,
        "role-code" to roleCode,
        "caseload" to caseloadId,
        "admin" to authenticationFacade.currentUsername
      ),
      null
    )
  }

  private fun toUserDetail(user: UserPersonDetail): UserDetail {
    val accountDetail = accountDetailRepository.findById(user.username).orElse(AccountDetail(username = user.username))
    return UserDetail(
      user,
      accountDetail,
      isAccountNonLocked(accountDetail.status),
      isCredentialsNonExpired(accountDetail),
      isEnabled(user, accountDetail.status),
      isAdmin(accountDetail),
      isActive(user)
    )
  }

  fun isAccountNonLocked(accountStatus: AccountStatus): Boolean =
    EnumSet.of(AccountStatus.OPEN, AccountStatus.EXPIRED, AccountStatus.EXPIRED_GRACE)
      .contains(accountStatus)

  fun isCredentialsNonExpired(accountDetail: AccountDetail): Boolean {
    val statusNonExpired =
      !EnumSet.of(AccountStatus.EXPIRED, AccountStatus.EXPIRED_LOCKED, AccountStatus.EXPIRED_LOCKED_TIMED).contains(
        accountDetail.status
      )
    val passwordExpiry = accountDetail.passwordExpiry
    return statusNonExpired && (passwordExpiry == null || passwordExpiry.isAfter(LocalDateTime.now()))
  }

  fun isEnabled(user: UserPersonDetail, accountStatus: AccountStatus): Boolean {
    return user.staff.isActive && EnumSet.of(AccountStatus.OPEN, AccountStatus.EXPIRED, AccountStatus.EXPIRED_GRACE)
      .contains(accountStatus)
  }

  fun isAdmin(accountDetail: AccountDetail): Boolean = accountDetail.accountProfile === AccountProfile.TAG_ADMIN

  fun isActive(user: UserPersonDetail): Boolean = user.staff.isActive

  private fun checkIfAccountAlreadyExists(username: String) {
    userPersonDetailRepository.findById(username.uppercase())
      .ifPresent { throw UserAlreadyExistsException("User $username already exists") }
  }

  private fun createStaffRecord(firstName: String, lastName: String, email: String): Staff {
    val staffAccount = Staff(
      firstName = firstName.uppercase(),
      lastName = lastName.uppercase(),
      status = "ACTIVE"
    )
    staffAccount.setEmail(email)
    return staffAccount
  }

  private fun createUserAccount(
    staffAccount: Staff,
    username: String,
    defaultCaseloadId: String,
    admin: Boolean = false,
    laaAdmin: Boolean = false
  ): UserPersonDetail {

    val userPersonDetail = UserPersonDetail(
      username = username.uppercase(),
      staff = staffAccount,
      type = getUsageType(admin)
    )
    caseloadRepository.findById(DPS_CASELOAD)
      .ifPresent {
        userPersonDetail.addCaseload(it)
      }

    val defaultCaseload = caseloadRepository.findById(defaultCaseloadId)
      .orElseThrow(CaseloadNotFoundException("Caseload $defaultCaseloadId not found"))
    userPersonDetail.addCaseload(defaultCaseload)

    userPersonDetail.setDefaultCaseload(defaultCaseload.id)

    if (admin && laaAdmin) {
      userPersonDetail.addToAdminUserGroup(defaultCaseload)
    }

    userPersonDetailRepository.saveAndFlush(userPersonDetail)
    return userPersonDetail
  }

  private fun createSchemaUser(
    username: String,
    profile: AccountProfile
  ) {
    userPersonDetailRepository.createUser(username, generatePassword(), profile.name)
    userPersonDetailRepository.expirePassword(username)
  }

  fun authenticateUser(username: String, password: String): Boolean =
    passwordEncoder.matches(password, userPasswordRepository.findByIdOrNull(username)?.password)
}

private fun Pageable.withSort(sortMapper: (sortProperty: String) -> String): Pageable {
  return PageRequest.of(this.pageNumber, this.pageSize, mapSortOrderProperties(this.sort, sortMapper))
}

private fun mapSortOrderProperties(sort: Sort, sortMapper: (sortProperty: String) -> String): Sort = by(
  sort
    .stream()
    .map { order: Order ->
      Order
        .by(sortMapper(order.property))
        .with(order.direction)
    }
    .collect(Collectors.toList())
)

class UserNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<UserNotFoundException> {
  override fun get(): UserNotFoundException {
    return UserNotFoundException(message)
  }
}

class UserAlreadyExistsException(message: String?) :
  RuntimeException(message),
  Supplier<UserAlreadyExistsException> {
  override fun get(): UserAlreadyExistsException {
    return UserAlreadyExistsException(message)
  }
}

class CaseloadNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<CaseloadNotFoundException> {
  override fun get(): CaseloadNotFoundException {
    return CaseloadNotFoundException(message)
  }
}

class PasswordTooShortException(message: String?) :
  RuntimeException(message),
  Supplier<PasswordTooShortException> {
  override fun get(): PasswordTooShortException {
    return PasswordTooShortException(message)
  }
}

class PasswordValidationException(message: String) : RuntimeException(message)

class ReusedPasswordException(message: String) : RuntimeException(message)
