package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.dto.CreateUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class UserResourceIntTest : IntegrationTestBase() {

  @DisplayName("GET /users/{username}")
  @Nested
  inner class GetUserByUsername {
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

    @Test
    fun `create user`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(BodyInserters.fromValue(CreateUserRequest("testuser2", "password123", "Test", "User")))
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "testuser2",
          "firstName": "Test",
          "lastName": "User"
          }
          """
        )
    }
  }
}
