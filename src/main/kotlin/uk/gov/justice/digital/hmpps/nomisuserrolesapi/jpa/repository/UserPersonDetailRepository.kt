package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@Suppress("SqlResolve")
@Repository
interface UserPersonDetailRepository :
  JpaRepository<UserPersonDetail, String>,
  JpaSpecificationExecutor<UserPersonDetail> {

  @Modifying
  @Query(value = "call oms_utils.create_user(:username, :password, :profile)", nativeQuery = true)
  fun createUser(username: String, password: String, profile: String = AccountProfile.TAG_GENERAL.name)

  @Modifying
  @Query(value = "call oms_utils.expire_password(:username)", nativeQuery = true)
  fun expirePassword(username: String)

  @Modifying
  @Query(value = "call oms_utils.drop_user(:username)", nativeQuery = true)
  fun dropUser(username: String)
}
