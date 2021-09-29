package uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper

import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAAdminUser
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAAdminUserPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAGeneralUser
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAGeneralUserPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.LocalAdminAuthorityRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.time.LocalDate

class GeneralUserBuilder(
  private val repository: UserPersonDetailRepository,
  private val localAdminRepository: LocalAdminAuthorityRepository,
  private var userPersonDetail: UserPersonDetail,
  private var prisonCodes: List<String>,
) {

  private fun generalUsersOf(prisonCodes: List<String>): List<LAAGeneralUser> {
    return prisonCodes.map {
      LAAGeneralUser(
        LAAGeneralUserPk(it, this.userPersonDetail.username),
        active = true,
        startDate = LocalDate.now().minusDays(1),
        authority = localAdminRepository.findByIdOrNull(it)!!,
        user = this.userPersonDetail
      )
    }
  }

  fun save() {
    repository.saveAndFlush(
      userPersonDetail.copy(
        administeredLinks = generalUsersOf(prisonCodes),
        type = "GENERAL",
        activeCaseLoadId = prisonCodes.first()
      )
    )
  }

  fun username(username: String): GeneralUserBuilder {
    this.userPersonDetail = userPersonDetail.copy(username = username)
    return this
  }

  fun atPrisons(prisonCodes: List<String>): GeneralUserBuilder {
    this.prisonCodes = prisonCodes
    return this
  }

  fun atPrison(prisonCode: String): GeneralUserBuilder {
    this.prisonCodes = listOf(prisonCode)
    return this
  }
}

class LocalAdministratorBuilder(
  private val repository: UserPersonDetailRepository,
  private val localAdminRepository: LocalAdminAuthorityRepository,
  private var userPersonDetail: UserPersonDetail,
  private var prisonCodes: List<String>,
) {

  private fun adminUsersOf(prisonCodes: List<String>): List<LAAAdminUser> {
    return prisonCodes.map {
      LAAAdminUser(
        LAAAdminUserPk(it, this.userPersonDetail.username),
        active = true,
        authority = localAdminRepository.findByIdOrNull(it)!!
      )
    }
  }

  fun save() {
    repository.saveAndFlush(
      userPersonDetail.copy(
        administratorLinks = adminUsersOf(prisonCodes),
        type = "ADMIN",
        activeCaseLoadId = prisonCodes.first()
      )
    )
  }

  fun username(username: String): LocalAdministratorBuilder {
    this.userPersonDetail = userPersonDetail.copy(username = username)
    return this
  }

  fun atPrisons(prisonCodes: List<String>): LocalAdministratorBuilder {
    this.prisonCodes = prisonCodes
    return this
  }

  fun atPrison(prisonCode: String): LocalAdministratorBuilder {
    this.prisonCodes = listOf(prisonCode)
    return this
  }
}

fun generalUserEntityCreator(
  repository: UserPersonDetailRepository,
  localAdminRepository: LocalAdminAuthorityRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): GeneralUserBuilder {
  return GeneralUserBuilder(repository, localAdminRepository, userPersonDetail, prisonCodes)
}

fun localAdministratorEntityCreator(
  repository: UserPersonDetailRepository,
  localAdminRepository: LocalAdminAuthorityRepository,
  userPersonDetail: UserPersonDetail = defaultPerson(),
  prisonCodes: List<String> = listOf("WWI"),
): LocalAdministratorBuilder {
  return LocalAdministratorBuilder(repository, localAdminRepository, userPersonDetail, prisonCodes)
}

fun defaultPerson(): UserPersonDetail {
  return UserPersonDetail(
    username = "tony",
    staff = Staff(firstName = "John", lastName = "Smith", status = "ACTIVE"),
    type = "GENERAL"
  )
}
