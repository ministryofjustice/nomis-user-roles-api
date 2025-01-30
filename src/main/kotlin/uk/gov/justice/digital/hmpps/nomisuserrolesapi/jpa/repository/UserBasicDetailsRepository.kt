package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import java.util.Optional

@Repository
interface UserBasicDetailsRepository : JpaRepository<UserBasicPersonalDetail, String> {

  @Query(
    value =
    "SELECT sua.USERNAME as username, sua.WORKING_CASELOAD_ID as activeCaseloadId, du.ACCOUNT_STATUS as accountStatus, sua.STAFF_ID as staffId, sm.FIRST_NAME as firstName, sm.LAST_NAME as lastName" +
      " FROM STAFF_USER_ACCOUNTS sua " +
      "      LEFT JOIN  DBA_USERS du  ON sua.USERNAME = du.USERNAME  " +
      "     JOIN STAFF_MEMBERS sm ON sm.STAFF_ID = sua.STAFF_ID  " +
      "WHERE " +
      "     sua.USERNAME= :username",
    nativeQuery = true,
  )
  fun find(@Param("username") username: String): Optional<UserBasicPersonalDetail>
}

@Entity
data class UserBasicPersonalDetail(
  @Id
  val username: String,
  val accountStatus: String?,
  val activeCaseloadId: String?,
  val firstName: String,
  val lastName: String,
  val staffId: Long,

) {
  val status: AccountStatus?
    get() = accountStatus?.let { AccountStatus.get(it) }

  private fun isLocked(): Boolean = AccountStatus.values().filter { it.isLocked }.contains(status)

  private fun isAccountNonLocked(): Boolean = !isLocked()

  fun isEnabled(): Boolean = isAccountNonLocked()
}
