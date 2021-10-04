package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@Repository
interface UserPersonDetailRepository : JpaRepository<UserPersonDetail, String> {
    @Modifying
    @Query(value = "call oms_utils.create_user(:username, :password)", nativeQuery = true)
    fun createUser(username: String, password: String)

    @Modifying
    @Query(value = "call oms_utils.drop_user(:username)", nativeQuery = true)
    fun dropUser(username: String)

}
