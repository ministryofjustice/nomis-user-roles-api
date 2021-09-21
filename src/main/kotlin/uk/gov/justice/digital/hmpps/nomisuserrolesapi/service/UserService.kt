package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource.UserDetail
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
}

class UserNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<UserNotFoundException> {
  override fun get(): UserNotFoundException {
    return UserNotFoundException(message)
  }
}
