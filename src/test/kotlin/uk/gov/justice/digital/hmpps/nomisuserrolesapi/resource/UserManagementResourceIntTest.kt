package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.BASIC_VALIDATION_FAILURE
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.PASSWORD_HAS_BEEN_USED_BEFORE
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.PASSWORD_NOT_ACCEPTABLE
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.lang.RuntimeException
import java.sql.SQLException

class UserManagementResourceIntTest : IntegrationTestBase() {

  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @SpyBean
  private lateinit var userPersonDetailRepository: UserPersonDetailRepository

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
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
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
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
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
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(BodyInserters.fromValue("testemail@"))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Validation failure: changeEmail.email: Invalid email address")
    }
  }

  @Nested
  @DisplayName("PUT /users/{username}/change-password")
  inner class ChangePassword {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("JMULLARD_GEN")
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    internal fun `must have role ROLE_MANAGE_NOMIS_USER_ACCOUNT`() {

      webTestClient.put().uri("/users/JMULLARD_GEN/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(BodyInserters.fromValue("hdhshshhhabad73hde"))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    internal fun `can change password when there are no errors returned from NOMIS`() {
      doNothing().whenever(userPersonDetailRepository).changePassword(any(), any())

      webTestClient.put().uri("/users/JMULLARD_GEN/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("hdhshshhhabad73hde"))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    internal fun `changing the password to old passwords recorded by NOMIS results in 400 error`() {
      doThrow(JpaSystemException(RuntimeException(SQLException("Password has been used before", "SQLSTATE", 20087)))).whenever(userPersonDetailRepository).changePassword(any(), any())

      webTestClient.put().uri("/users/JMULLARD_GEN/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("hdhshshhhabad73hde"))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Password has been used before and was rejected by NOMIS due to Password has been used before")
        .jsonPath("errorCode").isEqualTo(PASSWORD_HAS_BEEN_USED_BEFORE)
    }

    @Test
    internal fun `changing the password to one not compatible with NOMIS rules results in 400 error`() {
      doThrow(JpaSystemException(RuntimeException(SQLException("Password can not contain password", "SQLSTATE", 20001)))).whenever(userPersonDetailRepository).changePassword(any(), any())

      webTestClient.put().uri("/users/JMULLARD_GEN/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("hdhshshhhabad73hde"))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Password is not valid and has been rejected by NOMIS due to Password can not contain password")
        .jsonPath("errorCode").isEqualTo(PASSWORD_NOT_ACCEPTABLE)
    }

    @Test
    internal fun `changing the password that fails basic validation results in 400 error`() {
      webTestClient.put().uri("/users/JMULLARD_GEN/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("X"))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Validation failure: changePassword.password: Password must consist of alphanumeric characters only and a minimum of 14 chars, and max 30 chars")
        .jsonPath("errorCode").isEqualTo(BASIC_VALIDATION_FAILURE)
    }

    @Test
    internal fun `any unexpected NOMIS error results in a 500 error`() {
      doThrow(JpaSystemException(RuntimeException(SQLException("Bind error", "SQLSTATE", -803)))).whenever(userPersonDetailRepository).changePassword(any(), any())

      webTestClient.put().uri("/users/JMULLARD_GEN/change-password")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .body(BodyInserters.fromValue("hdhshshhhabad73hde"))
        .exchange()
        .expectStatus().is5xxServerError
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
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
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
    fun `can't change name of a user for an invalid first name`() {
      webTestClient.put().uri("/users/TEST_DATA_USER1/change-name")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            NameDetail(
              firstName = "87234gjhsdbfsdfh23r23f23g23",
              lastName = "lastName",
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Validation failure: First name must consist of alphabetical characters only and a max 35 chars")
    }

    @Test
    fun `can't change name of a user for an invalid last name`() {
      webTestClient.put().uri("/users/TEST_DATA_USER1/change-name")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            NameDetail(
              firstName = "firstname",
              lastName = "sdifhosidfhjoisdjfoiwejfoiwjefwefwefrdrd",
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Validation failure: Last name must consist of alphabetical characters only and a max 35 chars")
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
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
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
