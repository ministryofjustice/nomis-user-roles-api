package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class UserResourceIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

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
  }

  @DisplayName("GET /users/")
  @Nested
  inner class GetUser {
    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/users")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/users")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {
      webTestClient.get().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Nested
    @DisplayName("when called by a national central administrator")
    inner class NationalCentralAdministrator {
      @Test
      fun `a central admin user can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES_ADMIN role`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
      }
    }

    @Nested
    @DisplayName("when called by a local administrator")
    inner class LocalAdministrator {
      @BeforeEach
      internal fun createUsers() {
        with(dataBuilder) {
          localAdministrator().username("jane.lsa.wwi").atPrison("WWI").buildAndSave()

          generalUser().username("abella.moulin").firstName("Abella").lastName("Moulin").atPrison("WWI").buildAndSave()
          generalUser().username("marco.rossi").atPrisons(listOf("WWI", "BXI")).inactive().buildAndSave()
          generalUser().username("mark.bowlan").atPrison("BXI").buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() =
        dataBuilder.deleteUsers("jane.lsa.wwi", "abella.moulin", "marco.rossi", "mark.bowlan")

      @Test
      fun `a local administrator user can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES role`() {
        val matchByUserName = "$.content[?(@.username == '%s')]"

        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName + "active", "abella.moulin").isEqualTo(true)
          .jsonPath(matchByUserName + "firstName", "abella.moulin").isEqualTo("Abella")
          .jsonPath(matchByUserName + "lastName", "abella.moulin").isEqualTo("Moulin")
          .jsonPath(matchByUserName + "staffId", "abella.moulin").exists()
      }
    }
  }
}
