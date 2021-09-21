package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserService

class UserResourceTest {

  private val userService: UserService = mock()
  private val userResource = UserResource(userService)

  @Test
  fun `Get user details`() {
    val user = UserDetail("testuser1", 1, "John", "Smith")

    whenever(userService.findByUsername(any())).thenReturn(user)

    val userDetails = userResource.getUserDetails("testuser1")
    assertThat(userDetails).isEqualTo(
      UserDetail("testuser1", 1, "John", "Smith")
    )
  }

}
