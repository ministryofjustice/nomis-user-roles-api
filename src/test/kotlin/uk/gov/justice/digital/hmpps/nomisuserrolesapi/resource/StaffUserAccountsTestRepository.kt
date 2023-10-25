package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import java.time.LocalDateTime

/**
 * This repository exists to enable testing recording the last_logon_date.
 * I have had to avoid explicitly adding it to the UserPersonDetail entity because this field has not
 * been rolled out to production yet and Hibernate blew up when it wasn't there.
 */
@Repository
interface StaffUserAccountsTestRepository : CrudRepository<UserPersonDetail, Long> {
  @Query("SELECT last_logon_date FROM staff_user_accounts WHERE username = ?", nativeQuery = true)
  fun getLastLogonDateFor(username: String): LocalDateTime
}
