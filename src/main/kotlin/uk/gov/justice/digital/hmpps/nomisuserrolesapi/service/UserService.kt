package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import UserSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.data.domain.Sort.by
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.getUsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.AccountDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.mapUserSummarySortProperties
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toUserSummary
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.transaction.Transactional

@Service
@Transactional
class UserService(
  private val userPersonDetailRepository: UserPersonDetailRepository,
  private val caseloadRepository: CaseloadRepository,
  private val accountDetailRepository: AccountDetailRepository,
  private val staffRepository: StaffRepository
) {
  fun findByUsername(username: String): UserDetail =
    userPersonDetailRepository.findById(username)
      .map { u -> UserDetail(u, accountDetailRepository.findById(username).orElse(AccountDetail(username = username))) }
      .orElseThrow(UserNotFoundException("User $username not found"))

  fun findUsersByFilter(pageRequest: Pageable, filter: UserFilter): Page<UserSummary> =
    userPersonDetailRepository.findAll(UserSpecification(filter), pageRequest.withSort(::mapUserSummarySortProperties))
      .map { it.toUserSummary() }

  fun createGeneralUser(createUserRequest: CreateGeneralUserRequest): UserSummary {

    checkIfAccountAlreadyExists(createUserRequest.username)

    val staffAccount = createStaffRecord(createUserRequest.firstName, createUserRequest.lastName, createUserRequest.email)

    if (staffAccount.generalAccount() != null) {
      throw UserAlreadyExistsException("General user already exists for this staff member")
    }

    val userPersonDetail = createUserAccount(createUserRequest.username, createUserRequest.defaultCaseloadId, false, staffAccount)

    createSchemaUser(createUserRequest.username, AccountProfile.TAG_GENERAL)
    return userPersonDetail.toUserSummary()
  }

  fun linkGeneralAccount(linkedUsername: String, linkedUserRequest: CreateLinkedGeneralUserRequest): UserSummary {
    checkIfAccountAlreadyExists(linkedUserRequest.username)

    val staffAccount = userPersonDetailRepository.findById(linkedUsername).map { it.staff }
      .orElseThrow { UserNotFoundException("Linked User Account $linkedUsername not found") }

    if (staffAccount.generalAccount() != null) {
      throw UserAlreadyExistsException("General user already exists for this staff member")
    }

    val userPersonDetail = createUserAccount(linkedUserRequest.username, linkedUserRequest.defaultCaseloadId, false, staffAccount)
    createSchemaUser(linkedUserRequest.username, AccountProfile.TAG_GENERAL)

    return userPersonDetail.toUserSummary()
  }

  fun createAdminUser(createUserRequest: CreateAdminUserRequest): UserSummary {

    checkIfAccountAlreadyExists(createUserRequest.username)

    val staffAccount = createStaffRecord(createUserRequest.firstName, createUserRequest.lastName, createUserRequest.email)

    if (staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail = createUserAccount(createUserRequest.username, "CADM_I", true, staffAccount)

    createSchemaUser(createUserRequest.username, AccountProfile.TAG_ADMIN)

    return userPersonDetail.toUserSummary()
  }

  fun linkAdminAccount(linkedUsername: String, linkedUserRequest: CreateLinkedAdminUserRequest): UserSummary {
    checkIfAccountAlreadyExists(linkedUserRequest.username)

    val staffAccount = userPersonDetailRepository.findById(linkedUsername).map { it.staff }
      .orElseThrow { UserNotFoundException("Linked User Account $linkedUsername not found") }

    if (staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail = createUserAccount(linkedUserRequest.username, "CADM_I", true, staffAccount)
    createSchemaUser(linkedUserRequest.username, AccountProfile.TAG_ADMIN)

    return userPersonDetail.toUserSummary()
  }

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
    username: String,
    defaultCaseloadId: String,
    admin: Boolean,
    staffAccount: Staff
  ): UserPersonDetail {
    val userPersonDetail = UserPersonDetail(
      username = username.uppercase(),
      staff = staffAccount,
      type = getUsageType(admin)
    )
    caseloadRepository.findById("NWEB")
      .ifPresent {
        userPersonDetail.addCaseload(it)
      }

    val defaultCaseload = caseloadRepository.findById(defaultCaseloadId)
      .orElseThrow(CaseloadNotFoundException("Caseload $defaultCaseloadId not found"))
    userPersonDetail.addCaseload(defaultCaseload)

    userPersonDetail.activeCaseLoad = defaultCaseload
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

  fun deleteUser(username: String) {
    val userPersonDetail = userPersonDetailRepository.findById(username)
      .orElseThrow(UserNotFoundException("User $username not found"))

    userPersonDetailRepository.delete(userPersonDetail)
    userPersonDetailRepository.dropUser(username)
  }

  fun findByStaffId(staffId: Long): StaffDetail {
    return staffRepository.findById(staffId)
      .map { s -> StaffDetail(s) }
      .orElseThrow(UserNotFoundException("Staff ID $staffId not found"))
  }

  fun lockUser(username: String) {
    userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    userPersonDetailRepository.lockUser(username)
  }

  fun unlockUser(username: String) {
    userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    userPersonDetailRepository.unlockUser(username)
  }

  fun changePassword(username: String, password: String) {
    userPersonDetailRepository.findById(username).orElseThrow(UserNotFoundException("User $username not found"))
    userPersonDetailRepository.changePassword(username, password)
  }
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
