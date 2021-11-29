package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummaryWithEmail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

class UserResourceTest {

  private val userService: UserService = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val userResource = UserResource(userService, authenticationFacade)

  private val user = UserDetail(
    username = "testuser1",
    staffId = 1,
    firstName = "John",
    lastName = "Smith",
    active = true,
    activeCaseloadId = "BXI",
    accountStatus = AccountStatus.OPEN,
    primaryEmail = "test@test.com",
    dpsRoleCodes = listOf("ROLE_GLOBAL_SEARCH", "ROLE_ROLES_ADMIN")
  )

  @Test
  fun `Get user details`() {
    whenever(userService.findByUsername(any())).thenReturn(user)

    val userDetails = userResource.getUserDetails("testuser1")
    assertThat(userDetails).isEqualTo(user)
  }

  @Test
  fun `Get user by first name and last name`() {
    val user = UserSummaryWithEmail(
      username = "testuser1",
      staffId = 1,
      firstName = "John",
      lastName = "Smith",
      active = true,
      activeCaseload = PrisonCaseload("MDI", "Moorland"),
      email = "joe@bloggs.com",
    )

    whenever(userService.findUsersByFirstAndLastNames(any(), any())).thenReturn(listOf(user))

    val userDetails = userResource.findUsersByFirstAndLastNames("John", "Smith")
    assertThat(userDetails).containsExactly(user)
  }

  @Test
  fun `Get user by emailAddress`() {
    whenever(userService.findAllByEmailAddress(any())).thenReturn(listOf(user))

    val userDetails = userResource.findUsersByEmailAddress("test@test.com")
    assertThat(userDetails).containsExactly(user)
  }

  @Test
  fun `Get user by emailAddress and usernames`() {
    whenever(userService.findAllByEmailAddressAndUsernames(any(), any())).thenReturn(listOf(user))

    val userDetails = userResource.findUsersByEmailAddressAndUsernames("test@test.com", listOf("bob", "fred"))
    assertThat(userDetails).containsExactly(user)
  }
}
