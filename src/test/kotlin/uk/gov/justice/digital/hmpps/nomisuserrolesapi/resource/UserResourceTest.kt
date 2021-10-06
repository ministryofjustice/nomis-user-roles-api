package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

class UserResourceTest {

  private val userService: UserService = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val userResource = UserResource(userService, authenticationFacade)

  @Test
  fun `Get user details`() {
    val user = UserDetail(
      username = "testuser1",
      staffId = 1,
      firstName = "John",
      lastName = "Smith",
      active = true,
      activeCaseloadId = "BXI",
      accountStatus = AccountStatus.OPEN,
      primaryEmail = "test@test.com"
    )

    whenever(userService.findByUsername(any())).thenReturn(user)

    val userDetails = userResource.getUserDetails("testuser1")
    assertThat(userDetails).isEqualTo(user)
  }
}
