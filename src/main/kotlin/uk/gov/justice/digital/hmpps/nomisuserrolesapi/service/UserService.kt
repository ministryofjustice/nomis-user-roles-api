package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import UserFilter
import UserSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toUserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto.UserDetail
import java.util.function.Supplier
import javax.transaction.Transactional

@Service
@Transactional
class UserService(
  private val userPersonDetailRepository: UserPersonDetailRepository
) {
  fun findByUsername(username: String): UserDetail =
    userPersonDetailRepository.findById(username)
      .map { u -> UserDetail(u.username, u.staff.staffId, u.staff.firstName, u.staff.lastName) }
      .orElseThrow(UserNotFoundException("User $username not found"))
  }

  fun createUser(username: String, password: String, firstName: String, lastName: String) : UserDetail {
    userPersonDetailRepository.createUser(username, password)

    val userPersonDetail = UserPersonDetail(
      username = username,
      staff = Staff(firstName = firstName, lastName = lastName, status = "ACTIVE"),
      type = "GENERAL"
    )

    userPersonDetailRepository.saveAndFlush(userPersonDetail)
    return UserDetail(userPersonDetail)
  }

  fun deleteUser(username: String) {
    val userPersonDetail = userPersonDetailRepository.findById(username)
      .orElseThrow(UserNotFoundException("User $username not found"))

    userPersonDetailRepository.delete(userPersonDetail)
    userPersonDetailRepository.dropUser(username)
 }


  fun getLocalUsers(pageRequest: Pageable, filter: UserFilter): Page<UserSummary> =
    userPersonDetailRepository.findAll(UserSpecification(filter), pageRequest)
      .map { it.toUserSummary() }
}

class UserNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<UserNotFoundException> {
  override fun get(): UserNotFoundException {
    return UserNotFoundException(message)
  }
}
