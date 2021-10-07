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
          generalUser().username("abella.moulin").firstName("ABELLA").lastName("MOULIN").atPrison("WWI").buildAndSave()
          generalUser().username("marco.rossi").firstName("MARCO").lastName("ROSSI").atPrisons(listOf("WWI", "BXI")).inactive().buildAndSave()
          generalUser().username("mark.bowlan").firstName("MARK").lastName("BOWLAN").atPrison("BXI").buildAndSave()
          generalUser().username("ella.dribble").firstName("ELLA").lastName("DRIBBLE").atPrisons(listOf("BXI", "WWI")).inactive().buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      fun `they can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES_ADMIN role`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(4)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }
      @Test
      fun `blank filters are ignored`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("nameFilter", "")
            .queryParam("caseload", "")
            .queryParam("activeCaseload", "")
            .queryParam("accessRoles", "")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(4)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      } @Test
      fun `they can filter by user name`() {
        webTestClient.get().uri { it.path("/users/").queryParam("nameFilter", "mar").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
      }
      @Test
      fun `they can filter by account status`() {
        webTestClient.get().uri { it.path("/users/").queryParam("status", "ACTIVE").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
      }
      @Test
      fun `invalid account status filter is a bad request`() {
        webTestClient.get().uri { it.path("/users/").queryParam("status", "INACT").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isBadRequest
      }
      @Test
      fun `they can filter by active caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("activeCaseload", "WWI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
      }
      @Test
      fun `they can filter by caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("caseload", "WWI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(3)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }
    }

    @Nested
    @DisplayName("when called by a local administrator")
    inner class LocalAdministrator {
      @BeforeEach
      internal fun createUsers() {
        with(dataBuilder) {
          localAdministrator().username("jane.lsa.wwi").atPrison("WWI").buildAndSave()

          generalUser().username("abella.moulin").firstName("ABELLA").lastName("MOULIN").atPrison("WWI").buildAndSave()
          // first prison is set to active caseload
          generalUser().username("marco.rossi").firstName("MARCO").lastName("ROSSI").atPrisons(listOf("BXI", "WWI")).inactive().buildAndSave()
          generalUser().username("ella.dribble").firstName("ELLA").lastName("DRIBBLE").atPrisons(listOf("WWI", "BXI")).inactive().buildAndSave()
          generalUser().username("mark.bowlan").firstName("MARK").lastName("BOWLAN").atPrison("BXI").buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      fun `they can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES role`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(3)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
          .jsonPath(matchByUserName + "active", "abella.moulin").isEqualTo(true)
          .jsonPath(matchByUserName + "firstName", "abella.moulin").isEqualTo("ABELLA")
          .jsonPath(matchByUserName + "lastName", "abella.moulin").isEqualTo("MOULIN")
          .jsonPath(matchByUserName + "staffId", "abella.moulin").exists()
          .jsonPath(matchByUserName + "activeCaseload.id", "abella.moulin").isEqualTo("WWI")
          .jsonPath(matchByUserName + "activeCaseload.name", "abella.moulin").isEqualTo("WANDSWORTH (HMP)")
      }

      @Test
      internal fun `they would see all users, including themselves,  if they have the nation admin role`() {
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
          .jsonPath("$.numberOfElements").isEqualTo(5)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "jane.lsa.wwi").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }

      @Test
      fun `they can filter by user name`() {
        webTestClient.get().uri { it.path("/users/").queryParam("nameFilter", "mar").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(1)
          .jsonPath(matchByUserName, "marco.rossi").exists()
      }
      @Test
      fun `they can filter by account status`() {
        webTestClient.get().uri { it.path("/users/").queryParam("status", "ACTIVE").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(1)
          .jsonPath(matchByUserName, "abella.moulin").exists()
      }

      @Test
      fun `they can filter by active caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("activeCaseload", "BXI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(1)
          .jsonPath(matchByUserName, "marco.rossi").exists()
      }
      @Test
      fun `they can filter by caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("caseload", "BXI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
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
          "username": "TESTUSER2",
          "firstName": "TEST",
          "lastName": "USER",
          "activeCaseloadId" : "BXI",
          "active": true,
          "accountStatus": "EXPIRED",
          "primaryEmail": "test@test.com",
          "accountType": "ADMIN"
          }
          """
        )

      webTestClient.get().uri("/users/TESTUSER2")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("TESTUSER2")
        .jsonPath("firstName").isEqualTo("TEST")
        .jsonPath("lastName").isEqualTo("USER")
        .jsonPath("staffId").exists()
    }

    @Test
    fun `a user can be link to an existing account`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser4",
              password = "password123456",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "CADM_I",
              email = "test@test.com",
              adminUser = true
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser5",
              password = "password123",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com",
              adminUser = false,
              linkedUsername = "TESTUSER4"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      val responseSpec = webTestClient.get().uri("/users/TESTUSER4")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `a user cannot be link to an existing account of same ADMIN type`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser6",
              password = "password123456",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "CADM_I",
              email = "test@test.com",
              adminUser = true
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser5",
              password = "password123456",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "CADM_I",
              email = "test@test.com",
              adminUser = true,
              linkedUsername = "TESTUSER6"
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
    }

    @Test
    fun `a user cannot be link to an existing account of same GENERAL type`() {

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser7",
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

      webTestClient.post().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateUserRequest(
              username = "testuser8",
              password = "password123",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com",
              linkedUsername = "TESTUSER7"
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
    }
  }

  @Nested
  @DisplayName("DELETE /users/{username}")
  inner class DeleteUsers {

    @Test
    fun `can't drop a db user that doesn't exist`() {
      webTestClient.delete().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't drop a db user without correct role`() {
      webTestClient.delete().uri("/users/TESTUSER3")
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

      webTestClient.delete().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
