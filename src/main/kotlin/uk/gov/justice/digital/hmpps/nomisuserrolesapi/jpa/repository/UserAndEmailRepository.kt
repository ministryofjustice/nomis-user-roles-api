package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserAndEmailRepository : JpaRepository<UserAndEmail, String> {

  @Query(
    value =
    "SELECT username, LISTAGG(internet_address, ', ') WITHIN GROUP (ORDER BY owner_id) AS email " +
      "FROM STAFF_USER_ACCOUNTS sua " +
      "      INNER JOIN STAFF_MEMBERS sm ON sm.staff_id = sua.staff_id " +
      "      INNER JOIN internet_addresses ia ON  sm.staff_id = ia.owner_id " +
      "WHERE " +
      "      ia.internet_address_class = 'EMAIL' AND ia.owner_class = 'STF' " +
      "GROUP BY username ORDER BY username",
    nativeQuery = true,
  )
  fun findUsersAndEmails(): List<UserAndEmail>
}

@Entity
data class UserAndEmail(
  @Id
  val username: String,
  val email: String? = null,
)
