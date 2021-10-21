package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class UserManagementResourceIntTest : IntegrationTestBase() {

  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @Nested
  @DisplayName("PUT /users/{username}/lock-user")
  inner class LockUnlockUsers {

    @BeforeEach
    internal fun createUsers() {
      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "LOCKING_USER1",
              firstName = "Locking",
              lastName = "User",
              email = "test@test.com",
              defaultCaseloadId = "PVI"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated
    }

    @AfterEach
    internal fun deleteUsers() {
      webTestClient.delete().uri("/users/LOCKING_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `can't lock a db user that doesn't exist`() {
      webTestClient.put().uri("/users/LOCKING_USER2/lock-user")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't lock a db user without correct role`() {
      webTestClient.put().uri("/users/LOCKING_USER1/lock-user")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can lock a db user that does exist`() {
      webTestClient.put().uri("/users/LOCKING_USER1/lock-user")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/users/LOCKING_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("accountStatus").isEqualTo("LOCKED")
    }

    @Test
    fun `can't unlock a db user that doesn't exist`() {
      webTestClient.put().uri("/users/LOCKING_USER2/unlock-user")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't unlock a db user without correct role`() {
      webTestClient.put().uri("/users/LOCKING_USER1/unlock-user")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can unlock a db user that does exist`() {
      webTestClient.put().uri("/users/LOCKING_USER1/unlock-user")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/users/LOCKING_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("accountStatus").isEqualTo("OPEN")
    }

    @Test
    fun `can't change password of a db user that doesn't exist`() {
      webTestClient.put().uri("/users/LOCKING_USER2/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("He110W0R1D5555"))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't change password of a db user without correct role`() {
      webTestClient.put().uri("/users/LOCKING_USER1/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(BodyInserters.fromValue("He110W0R1D5555"))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can change password of a  db user that does exist`() {
      webTestClient.put().uri("/users/LOCKING_USER1/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("He110W0R1D5555"))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `can't change password of a db user for an invalid password`() {
      webTestClient.put().uri("/users/LOCKING_USER1/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("HELLO^%$"))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Validation failure: changePassword.password: Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars")
    }
  }

  @Nested
  @DisplayName("PUT /users/{username}/change-email")
  inner class ChangeEmail {

    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("TEST_DATA_USER1")
          .firstName("TEST")
          .lastName("USER1")
          .email("test@test.com")
          .atPrisons(listOf("BXI"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `can't change email of a user that doesn't exist`() {
      webTestClient.put().uri("/users/TEST_DATA_USER2/change-email")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("newtest@test.com"))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't change email address of user without correct role`() {
      webTestClient.put().uri("/users/TEST_DATA_USER1/change-email")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(BodyInserters.fromValue("newtest@test.com"))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can change email address of a user that does exist`() {
      webTestClient.put().uri("/users/TEST_DATA_USER1/change-email")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("newtest@test.com"))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("primaryEmail").isEqualTo("newtest@test.com")

      webTestClient.get().uri("/users/TEST_DATA_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("primaryEmail").isEqualTo("newtest@test.com")
    }

    @Test
    fun `can't change email of a user for an invalid email`() {
      webTestClient.put().uri("/users/TEST_DATA_USER1/change-email")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("testemail@"))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Validation failure: changeEmail.email: Invalid email address")
    }
  }

  @Nested
  @DisplayName("PUT /users/{username}/change-name")
  inner class ChangeName {

    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("TEST_DATA_USER1")
          .firstName("TEST")
          .lastName("USER1")
          .email("test@test.com")
          .atPrisons(listOf("BXI"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `can't change name of a user that doesn't exist`() {
      webTestClient.put().uri("/users/TEST_DATA_USER2/change-name")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(
          BodyInserters.fromValue(
            NameDetail(
              firstName = "NewFirstName",
              lastName = "NewLastName",
            )
          )
        )
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't change name of user without correct role`() {
      webTestClient.put().uri("/users/TEST_DATA_USER1/change-name")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(
          BodyInserters.fromValue(
            NameDetail(
              firstName = "NewFirstName",
              lastName = "NewLastName",
            )
          )
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can change name of a user that does exist`() {

      webTestClient.get().uri("/users/TEST_DATA_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("lastName").isEqualTo("USER1")
        .jsonPath("firstName").isEqualTo("TEST")

      webTestClient.put().uri("/users/TEST_DATA_USER1/change-name")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(
          BodyInserters.fromValue(
            NameDetail(
              firstName = "NewFirstName",
              lastName = "NewLastName",
            )
          )
        )
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("lastName").isEqualTo("NEWLASTNAME")
        .jsonPath("firstName").isEqualTo("NEWFIRSTNAME")

      webTestClient.get().uri("/users/TEST_DATA_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("lastName").isEqualTo("NEWLASTNAME")
        .jsonPath("firstName").isEqualTo("NEWFIRSTNAME")
    }
  }
}
