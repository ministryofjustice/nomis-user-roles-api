package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import UserSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.AccountDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toUserSummary
import java.util.function.Supplier
import javax.persistence.EntityExistsException
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
    userPersonDetailRepository.findAll(UserSpecification(filter), pageRequest)
      .map { it.toUserSummary() }

  fun createUser(createUserRequest: CreateUserRequest): UserDetail {

    userPersonDetailRepository.findById(createUserRequest.username.uppercase()).ifPresent { throw UserAlreadyExistsException("User ${createUserRequest.username} already exists") }

    if (createUserRequest.adminUser && createUserRequest.password.length < 14) {
      throw PasswordTooShortException("Password must be at least 14 alpha-numeric characters in length. Please re-enter password.")
    }

    val type = if (createUserRequest.adminUser) {
      "ADMIN"
    } else {
      "GENERAL"
    }

    val staffAccount = createUserRequest.linkedUsername?.let { userAccount ->
      userPersonDetailRepository.findById(userAccount).map{it.staff}.orElseThrow{UserNotFoundException("Linked User Account $userAccount not found")}
    } ?: run {
      val staff = Staff(firstName = createUserRequest.firstName.uppercase(), lastName = createUserRequest.lastName.uppercase(), status = "ACTIVE")
      staff.setEmail(createUserRequest.email)
      staff
    }

    if (type == "GENERAL" && staffAccount.generalAccount() != null) {
      throw UserAlreadyExistsException("General user already exists for this staff member")
    }
    if (type == "ADMIN" && staffAccount.adminAccount() != null) {
      throw UserAlreadyExistsException("Admin user already exists for this staff member")
    }

    val userPersonDetail = UserPersonDetail(
      username = createUserRequest.username.uppercase(),
      staff = staffAccount,
      type = type
    )
    caseloadRepository.findById("NWEB")
      .ifPresent {
        userPersonDetail.addCaseload(it)
      }

    val defaultCaseload = caseloadRepository.findById(createUserRequest.defaultCaseloadId)
      .orElseThrow(CaseloadNotFoundException("Caseload ${createUserRequest.defaultCaseloadId} not found"))
    userPersonDetail.addCaseload(defaultCaseload)

    userPersonDetail.activeCaseLoad = defaultCaseload
    userPersonDetailRepository.saveAndFlush(userPersonDetail)

    val profile = if (createUserRequest.adminUser) {
      AccountProfile.TAG_ADMIN
    } else {
      AccountProfile.TAG_GENERAL
    }
    userPersonDetailRepository.createUser(createUserRequest.username, createUserRequest.password, profile.name)
    userPersonDetailRepository.expirePassword(createUserRequest.username)

    val status = accountDetailRepository.findById(createUserRequest.username)
      .orElseThrow(UserNotFoundException("User $createUserRequest.username not found"))

    return UserDetail(userPersonDetail, status)
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
}

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
