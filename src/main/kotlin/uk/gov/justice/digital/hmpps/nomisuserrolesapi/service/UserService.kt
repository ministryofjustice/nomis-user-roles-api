package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
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

  fun getLocalUsers(pageRequest: Pageable, localAdministratorUsername: String): Page<UserSummary> {
    val users = userPersonDetailRepository.findByIdOrNull(localAdministratorUsername)
      ?.let {
        it.administratorOfUserGroups
          .flatMap { links -> links.userGroup.members }
          .map { member ->
            UserSummary(
              username = member.user.username,
              staffId = member.user.staff.staffId,
              firstName = member.user.staff.firstName,
              lastName = member.user.staff.lastName,
              active = member.user.staff.isActive,
              activeCaseload = member.user.activeCaseLoad?.let { caseload ->
                PrisonCaseload(
                  id = caseload.id,
                  description = caseload.name
                )
              },
            )
          }
      } ?: listOf()
    return PageImpl(users, pageRequest, users.size.toLong())
  }
}

class UserNotFoundException(message: String?) :
  RuntimeException(message),
  Supplier<UserNotFoundException> {
  override fun get(): UserNotFoundException {
    return UserNotFoundException(message)
  }
}
