package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class UserCaseloadManagementResourceIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @DisplayName("GET /users/{username}/caseloads")
  @Nested
  inner class GetCaseloadsByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("CASELOAD_USER1")
          .firstName("CASELOAD")
          .lastName("USER1")
          .atPrisons(listOf("BXI", "WWI"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user with caseloads not found`() {

      webTestClient.get().uri("/users/dummy/caseloads")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `get user with caseloads`() {

      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "NWEB").doesNotExist()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "WWI").exists()
    }
  }

  @DisplayName("POST /users/{username}/caseloads/{caseloadId}")
  @Nested
  inner class AddCaseloadsByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("CASELOAD_USER1")
          .firstName("MARK")
          .lastName("BOWLAN")
          .atPrison("BXI")
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `add caseload to user forbidden with wrong role`() {

      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `add caseloads not found`() {

      webTestClient.post().uri("/users/dummy/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `add caseload to user`() {

      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "LEI").doesNotExist()

      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "LEI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "NWEB").exists()

      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "LEI").exists()

      webTestClient.delete().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "LEI").doesNotExist()

      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "LEI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "NWEB").exists()

      webTestClient.get().uri("/users/CASELOAD_USER1/caseloads")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "LEI").exists()
    }

    @Test
    fun `add none existing caseload to user`() {
      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/XXX")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Caseload not found: Caseload XXX not found")
    }

    @Test
    fun `add existing caseload to user`() {
      webTestClient.post().uri("/users/CASELOAD_USER1/caseloads/BXI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Caseload already exists: Caseload BXI already added to this user")
    }
  }

  @DisplayName("PUT /users/{username}/default-caseload/{defaultCaseloadId}")
  @Nested
  inner class SetDefaultCaseload {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("CASELOAD_USER1")
          .firstName("MARK")
          .lastName("BOWLAN")
          .atPrisons(listOf("BXI", "WWI"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.put().uri("/users/CASELOAD_USER1/default-caseload/WWI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.put().uri("/users/CASELOAD_USER1/default-caseload/WWI")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `set caseload default forbidden with wrong role`() {

      webTestClient.put().uri("/users/CASELOAD_USER1/default-caseload/WWI")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `set caseload default with user not found`() {

      webTestClient.put().uri("/users/dummy/default-caseload/WWI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `set user caseload`() {

      webTestClient.put().uri("/users/CASELOAD_USER1/default-caseload/WWI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("WWI")

      webTestClient.put().uri("/users/CASELOAD_USER1/default-caseload/BXI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
    }

    @Test
    fun `set user caseload not in caseload list`() {

      webTestClient.put().uri("/users/CASELOAD_USER1/default-caseload/MDI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Caseload not found: Default caseload cannot be set as user does not have MDI.")
    }
  }

  @DisplayName("DELETE /users/{username}/caseloads/{caseloadId}")
  @Nested
  inner class DeleteCaseloadByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("CASELOAD_USER1")
          .firstName("MARK")
          .lastName("BOWLAN")
          .atPrisons(listOf("BXI", "WWI"))
          .dpsRoles(listOf("APPROVE_CATEGORISATION", "CREATE_CATEGORISATION", "GLOBAL_SEARCH"))
          .nomisRoles(listOf("200"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.delete().uri("/users/CASELOAD_USER1/caseloads/WWI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.delete().uri("/users/CASELOAD_USER1/caseloads/WWI")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `delete caseload forbidden with wrong role`() {

      webTestClient.delete().uri("/users/CASELOAD_USER1/caseloads/WWI")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `delete user caseload with username not found`() {

      webTestClient.delete().uri("/user/dummy/caseloads/WWI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `delete caseload from user`() {
      webTestClient.delete().uri("/users/CASELOAD_USER1/caseloads/WWI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("CASELOAD_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "WWI").doesNotExist()
    }

    @Test
    fun `delete not existing caseload from user`() {
      webTestClient.delete().uri("/users/CASELOAD_USER1/caseloads/LEI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Caseload not found: Caseload cannot be removed as user does not have LEI.")
    }
  }
}
