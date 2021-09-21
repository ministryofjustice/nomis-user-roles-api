package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase


class UserControllerIntTest : IntegrationTestBase() {

  @Test
  fun `access forbidden when no authority`() {

    webTestClient.get().uri("/users/testuser1")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `access forbidden when no role`() {

    webTestClient.get().uri("/users/testuser1")
      .headers(setAuthorisation(roles = listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `get user`() {

    webTestClient.get().uri("/users/testuser1")
      .headers(setAuthorisation(roles = listOf("ROLE_USER_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectBody().json(
        """
          {
          "username": "testuser1",
          "staffId": 1,
          "firstName": "John",
          "lastName": "Smith"
          }
          """
      )
  }
}
