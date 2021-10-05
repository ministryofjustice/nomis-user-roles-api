package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class UserResourceIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @DisplayName("GET /users/{username}")
  @Nested
  inner class GetUserByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser().username("marco.rossi").firstName("Marco").lastName("Rossi").buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/users/marco.rossi")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.get().uri("/users/marco.rossi")
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

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("marco.rossi")
        .jsonPath("firstName").isEqualTo("Marco")
        .jsonPath("lastName").isEqualTo("Rossi")
        .jsonPath("staffId").exists()
    }
  }

  @DisplayName("GET /users/")
  @Nested
  inner class GetUser {
    val matchByUserName = "$.content[?(@.username == '%s')]"

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
      @BeforeEach
      internal fun createUsers() {
        with(dataBuilder) {
          generalUser().username("abella.moulin").firstName("Abella").lastName("Moulin").atPrison("WWI")
            .buildAndSave()
          generalUser().username("marco.rossi").atPrisons(listOf("WWI", "BXI")).inactive().buildAndSave()
          generalUser().username("mark.bowlan").atPrison("BXI").buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      fun `a central admin user can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES_ADMIN role`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(3)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
      }
    }

    @Nested
    @DisplayName("when called by a local administrator")
    inner class LocalAdministrator {
      @BeforeEach
      internal fun createUsers() {
        with(dataBuilder) {
          localAdministrator().username("jane.lsa.wwi").atPrison("WWI").buildAndSave()

          generalUser().username("abella.moulin").firstName("Abella").lastName("Moulin").atPrison("WWI")
            .buildAndSave()
          generalUser().username("marco.rossi").atPrisons(listOf("WWI", "BXI")).inactive().buildAndSave()
          generalUser().username("mark.bowlan").atPrison("BXI").buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      fun `a local administrator user can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES role`() {
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
          .jsonPath(matchByUserName + "activeCaseload.id", "abella.moulin").isEqualTo("WWI")
          .jsonPath(matchByUserName + "activeCaseload.name", "abella.moulin").isEqualTo("WANDSWORTH (HMP)")
      }

      @Test
      internal fun `a local administrator would see all users, including themselves,  if they have the nation admin role`() {
        webTestClient.get().uri("/users/")
          .headers(
            setAuthorisation(
              roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
              user = "jane.lsa.wwi"
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(4)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "jane.lsa.wwi").exists()
      }
    }
  }

  @Nested
  @DisplayName("POST /users")
  inner class MaintainUsers {

    @Test
    fun `a database user cannot be created without correct role`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser2",
              password = "password123",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `a database user cannot be created with invalid password`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser2",
              password = "password",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
    }

    @Test
    fun `a database user can be created`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser2",
              password = "password123456",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com",
              adminUser = true
            )
          )
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "testuser2",
          "firstName": "Test",
          "lastName": "User",
          "activeCaseloadId" : "BXI",
          "active": true,
          "accountStatus": "EXPIRED"
          }
          """
        )

      webTestClient.get().uri("/users/testuser2")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("testuser2")
        .jsonPath("firstName").isEqualTo("Test")
        .jsonPath("lastName").isEqualTo("User")
        .jsonPath("staffId").exists()
    }
  }

  @Nested
  @DisplayName("DELETE /users/{username}")
  inner class DeleteUsers {

    @Test
    fun `can't drop a db user that doesn't exist`() {
      webTestClient.delete().uri("/users/testuser3")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't drop a db user without correct role`() {
      webTestClient.delete().uri("/users/testuser3")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can drop a db user that does exist`() {
      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser3",
              password = "password123",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.delete().uri("/users/testuser3")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/users/testuser3")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
