package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto.UserDetail
import java.util.function.Supplier
import javax.transaction.Transactional

@Service
@Transactional
class UserService(
  private val userPersonDetailRepository: UserPersonDetailRepository
) {
  fun findByUsername(username: String): UserDetail {
    return userPersonDetailRepository.findById(username)
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

}

class UserNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<UserNotFoundException> {
  override fun get(): UserNotFoundException {
    return UserNotFoundException(message)
  }
}
