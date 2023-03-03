package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.PasswordValidationException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.ReusedPasswordException
import java.sql.SQLException

@Suppress("SqlResolve")
@Repository
interface UserPersonDetailRepository :
  JpaRepository<UserPersonDetail, String>,
  JpaSpecificationExecutor<UserPersonDetail> {

  @Modifying
  @Query(
    value = "call oms_utils.create_user(:username, :password, :profile)",
    nativeQuery = true,
    // see https://github.com/spring-projects/spring-data-jpa/issues/2812. Remove after upgrade past 2.7.9. Not used.
    countQuery = "select 1",
  )
  fun createUser(username: String, password: String, profile: String = AccountProfile.TAG_GENERAL.name)

  @Modifying
  @Query(
    value = "call oms_utils.expire_password(:username)",
    nativeQuery = true,
    // see https://github.com/spring-projects/spring-data-jpa/issues/2812. Remove after upgrade past 2.7.9. Not used.
    countQuery = "select 1",
  )
  fun expirePassword(username: String)

  @Modifying
  @Query(
    value = "call oms_utils.drop_user(:username)",
    nativeQuery = true,
    // see https://github.com/spring-projects/spring-data-jpa/issues/2812. Remove after upgrade past 2.7.9. Not used.
    countQuery = "select 1",
  )
  fun dropUser(username: String)

  @Modifying
  @Query(
    value = "call oms_utils.change_user_password(:username, :password)",
    nativeQuery = true,
    // see https://github.com/spring-projects/spring-data-jpa/issues/2812. Remove after upgrade past 2.7.9. Not used.
    countQuery = "select 1",
  )
  fun changePassword(username: String?, password: String?)

  @Modifying
  @Query(
    value = "call oms_utils.unlock_user(:username)",
    nativeQuery = true,
    // see https://github.com/spring-projects/spring-data-jpa/issues/2812. Remove after upgrade past 2.7.9. Not used.
    countQuery = "select 1",
  )
  fun unlockUser(username: String?)

  @Modifying
  @Query(
    value = "call oms_utils.lock_user(:username)",
    nativeQuery = true,
    // see https://github.com/spring-projects/spring-data-jpa/issues/2812. Remove after upgrade past 2.7.9. Not used.
    countQuery = "select 1",
  )
  fun lockUser(username: String?)

  @EntityGraph(value = "user-person-detail-download-graph", type = EntityGraph.EntityGraphType.LOAD)
  override fun findAll(speci: Specification<UserPersonDetail>?): List<UserPersonDetail>
  fun findAllByStaff_FirstNameIgnoreCaseAndStaff_LastNameIgnoreCase(
    firstName: String,
    lastName: String,
  ): List<UserPersonDetail>

  fun findByStaff_EmailsEmail(emailAddress: String): List<UserPersonDetail>
}

fun changePasswordWithValidation(
  username: String?,
  password: String?,
  changePassword: (username: String?, password: String?) -> Unit,
) {
  try {
    changePassword(username, password)
  } catch (e: Exception) {
    when (val cause = e.cause?.cause) {
      is SQLException -> when (cause.errorCode) {
        20001 -> throw PasswordValidationException("Password is not valid and has been rejected by NOMIS due to ${cause.message}")
        20087 -> throw ReusedPasswordException("Password has been used before and was rejected by NOMIS due to ${cause.message}")
      }
    }
    throw e
  }
}
