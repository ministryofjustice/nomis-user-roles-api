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
  fun `get user forbidden with wrong role`() {

    webTestClient.get().uri("/users/testuser1")
      .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `get user not found`() {

    webTestClient.get().uri("/users/dummy")
      .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get user`() {

    webTestClient.get().uri("/users/testuser1")
      .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
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