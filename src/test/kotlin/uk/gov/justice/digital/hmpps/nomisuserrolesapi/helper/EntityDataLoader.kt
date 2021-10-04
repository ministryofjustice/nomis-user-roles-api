package uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministrator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministratorPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMemberPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserGroupRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.time.LocalDate

class GeneralUserBuilder(
  repository: UserPersonDetailRepository,
  private val userGroupRepository: UserGroupRepository,
  private val caseloadRepository: CaseloadRepository,
  userPersonDetail: UserPersonDetail,
  prisonCodes: List<String>,
) : UserBuilder<GeneralUserBuilder>(
  repository = repository,
  userPersonDetail = userPersonDetail,
  prisonCodes = prisonCodes
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
    userPersonDetail =
      userPersonDetail.copy(
        activeAndInactiveMemberOfUserGroups = generalUsersOf(prisonCodes),
        type = "GENERAL",
        activeCaseLoad = prisonCodes.firstOrNull()?.let { caseloadRepository.findByIdOrNull(it) },
        caseloads = prisonCodes.map {
          UserCaseload(
            UserCaseloadPk(
              caseloadId = it,
              username = userPersonDetail.username
            ),
            startDate = LocalDate.now().minusDays(1),
            caseload = caseloadRepository.findByIdOrNull(it)!!,
            user = userPersonDetail
          )
        }
      )
    return this
  }
}

class LocalAdministratorBuilder(
  repository: UserPersonDetailRepository,
  private val userGroupRepository: UserGroupRepository,
  private val caseloadRepository: CaseloadRepository,
  userPersonDetail: UserPersonDetail,
  prisonCodes: List<String>,
) : UserBuilder<LocalAdministratorBuilder>(
  repository = repository,
  userPersonDetail = userPersonDetail,
  prisonCodes = prisonCodes
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
) {
  fun generalUser() = generalUserEntityCreator(repository, userGroupRepository, caseloadRepository)
  fun localAdministrator() = localAdministratorEntityCreator(repository, userGroupRepository, caseloadRepository)
  fun deleteAllUsers() {
    repository.deleteAll()
    repository.flush()
  }
}

fun generalUserEntityCreator(
  repository: UserPersonDetailRepository,
  userGroupRepository: UserGroupRepository,
  caseloadRepository: CaseloadRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): GeneralUserBuilder {
  return GeneralUserBuilder(repository, userGroupRepository, caseloadRepository, userPersonDetail, prisonCodes)
}

fun localAdministratorEntityCreator(
  repository: UserPersonDetailRepository,
  userGroupRepository: UserGroupRepository,
  caseloadRepository: CaseloadRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): LocalAdministratorBuilder {
  return LocalAdministratorBuilder(repository, userGroupRepository, caseloadRepository, userPersonDetail, prisonCodes)
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
  internal var userPersonDetail: UserPersonDetail,
  internal var prisonCodes: List<String>,
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

  fun inactive(): UserBuilder<T> {
    this.userPersonDetail = userPersonDetail.copy(staff = userPersonDetail.staff.copy(status = "INACT"))
    return this
  }
}
