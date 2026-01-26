package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLocalAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType

class UserAccountResourceIntTest : IntegrationTestBase() {

  @Nested
  @DisplayName("POST /users")
  inner class CreateUsers {

    @Test
    fun `an admin database user can be created`() {
      webTestClient.post().uri("/users/admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateAdminUserRequest(
              username = "testuser2",
              firstName = "Test",
              lastName = "U'ser",
              email = "test@test.com",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "TESTUSER2",
          "firstName": "Test",
          "lastName": "U'ser",
          "activeCaseloadId" : "CADM_I",
          "primaryEmail": "test@test.com",
          "accountType": "ADMIN"
          }
          """,
        )

      webTestClient.get().uri("/users/TESTUSER2")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("TESTUSER2")
        .jsonPath("firstName").isEqualTo("Test")
        .jsonPath("lastName").isEqualTo("U'ser")
        .jsonPath("staffId").exists()
    }

    @Test
    fun `a database user cannot be created without correct role`() {
      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "testuser-'2",
              firstName = "Test-User'",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com",
            ),
          ),
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `a local admin database user can be created`() {
      webTestClient.post().uri("/users/local-admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLocalAdminUserRequest(
              username = "laauser1",
              firstName = "Laa-ln'",
              lastName = "U'ser-ls",
              email = "laa@test.com",
              localAdminGroup = "PVI",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "LAAUSER1",
          "firstName": "Laa-ln'",
          "lastName": "U'ser-ls",
          "activeCaseloadId" : "CADM_I",
          "primaryEmail": "laa@test.com",
          "accountType": "ADMIN"
          }
          """,
        )
    }

    @Test
    fun `a general user can be created`() {
      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "testgenuser",
              firstName = "Test",
              lastName = "U'ser",
              defaultCaseloadId = "BXI",
              email = "testgen@test.com",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "TESTGENUSER",
          "firstName": "Test",
          "lastName": "U'ser",
          "activeCaseloadId" : "BXI",
          "primaryEmail": "testgen@test.com",
          "accountType": "GENERAL"
          }
          """,
        )
    }
  }

  @Nested
  @DisplayName("POST /users/link...")
  inner class LinkUsers {
    @Test
    fun `a local admin user can be linked to  general database user can local admin can search for general user`() {
      webTestClient.post().uri("/users/local-admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLocalAdminUserRequest(
              username = "laa_user1",
              firstName = "Laa",
              lastName = "User",
              email = "laa@test.com",
              localAdminGroup = "WWI",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "LAA_USER1",
          "firstName": "Laa",
          "lastName": "User",
          "activeCaseloadId" : "CADM_I",
          "primaryEmail": "laa@test.com",
          "accountType": "ADMIN"
          }
          """,
        )

      webTestClient.post().uri("/users/link-general-account/LAA_USER1")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedGeneralUserRequest(
              username = "generaluser1",
              defaultCaseloadId = "WWI",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("generalAccount.username").isEqualTo("GENERALUSER1")
        .jsonPath("generalAccount.activeCaseload.id").isEqualTo("WWI")
        .jsonPath("generalAccount.active").isEqualTo("false")
        .jsonPath("generalAccount.accountType").isEqualTo("GENERAL")
        .jsonPath("adminAccount.username").isEqualTo("LAA_USER1")
        .jsonPath("adminAccount.activeCaseload.id").isEqualTo("CADM_I")
        .jsonPath("adminAccount.active").isEqualTo("false")
        .jsonPath("adminAccount.accountType").isEqualTo("ADMIN")

      webTestClient.get().uri { it.path("/users").queryParam("nameFilter", "generaluser1").build() }
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), username = "LAA_USER1"))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.numberOfElements").isEqualTo(1)
        .jsonPath("$.content[?(@.username == '%s')]", "GENERALUSER1").exists()
    }

    @Test
    fun `a user can be link to an existing account`() {
      webTestClient.post().uri("/users/admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateAdminUserRequest(
              username = "testuser4",
              firstName = "Test-'fn",
              lastName = "User'-ln",
              email = "test@test.com",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated

      val staffDetail = webTestClient.post().uri("/users/link-general-account/TESTUSER4")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedGeneralUserRequest(
              username = "testuser5",
              defaultCaseloadId = "BXI",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody(StaffDetail::class.java)
        .returnResult().responseBody!!

      assertThat(staffDetail.firstName).isEqualTo("Test-'fn")
      assertThat(staffDetail.lastName).isEqualTo("User'-ln")
      assertThat(staffDetail.primaryEmail).isEqualTo("test@test.com")
      assertThat(staffDetail.status).isEqualTo("ACTIVE")

      assertThat(staffDetail.generalAccount).isNotNull
      assertThat(staffDetail.generalAccount?.username).isEqualTo("TESTUSER5")
      assertThat(staffDetail.generalAccount?.activeCaseload?.id).isEqualTo("BXI")
      assertThat(staffDetail.generalAccount?.active).isEqualTo(false)
      assertThat(staffDetail.generalAccount?.accountType).isEqualTo(UsageType.GENERAL)

      assertThat(staffDetail.adminAccount).isNotNull
      assertThat(staffDetail.adminAccount?.username).isEqualTo("TESTUSER4")
      assertThat(staffDetail.adminAccount?.activeCaseload?.id).isEqualTo("CADM_I")
      assertThat(staffDetail.adminAccount?.active).isEqualTo(false)
      assertThat(staffDetail.adminAccount?.accountType).isEqualTo(UsageType.ADMIN)

      webTestClient.get().uri("/users/staff/${staffDetail.staffId}")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("staffId").exists()
        .jsonPath("firstName").isEqualTo("Test-'fn")
        .jsonPath("lastName").isEqualTo("User'-ln")
        .jsonPath("primaryEmail").isEqualTo("test@test.com")
        .jsonPath("status").isEqualTo("ACTIVE")
        .jsonPath("generalAccount.username").isEqualTo("TESTUSER5")
        .jsonPath("generalAccount.activeCaseload.id").isEqualTo("BXI")
        .jsonPath("generalAccount.active").isEqualTo("false")
        .jsonPath("generalAccount.accountType").isEqualTo("GENERAL")
        .jsonPath("adminAccount.username").isEqualTo("TESTUSER4")
        .jsonPath("adminAccount.activeCaseload.id").isEqualTo("CADM_I")
        .jsonPath("adminAccount.active").isEqualTo("false")
        .jsonPath("adminAccount.accountType").isEqualTo("ADMIN")
    }

    @Test
    fun `a user cannot be link to an existing account of same ADMIN type`() {
      webTestClient.post().uri("/users/admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateAdminUserRequest(
              username = "testuser6",
              firstName = "Test",
              lastName = "User",
              email = "test@test.com",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users/link-admin-account/TESTUSER6")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedAdminUserRequest(
              username = "testuser5",
            ),
          ),
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("User already exists: Admin user already exists for this staff member")
    }

    @Test
    fun `a user cannot be link to an existing account of same GENERAL type`() {
      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "testuser7",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users/link-general-account/TESTUSER7")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedGeneralUserRequest(
              username = "testuser8",
              defaultCaseloadId = "BXI",
            ),
          ),
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("User already exists: General user already exists for this staff member")
    }
  }
}
