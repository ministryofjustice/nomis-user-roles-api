package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserLastNameRepository : JpaRepository<UserLastNameDetail, String> {

  @Query(
    value =
    "SELECT sua.username, sm.LAST_NAME as lastName FROM STAFF_USER_ACCOUNTS sua " +
      "      INNER JOIN STAFF_MEMBERS sm ON sm.staff_id = sua.staff_id ",

    nativeQuery = true,
  )
  fun findLastNamesAllUsers(): List<UserLastNameDetail>
}

@Entity
data class UserLastNameDetail(
  @Id
  val username: String,
  val lastName: String,
)
