package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class UserRoleManagementResourceIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @DisplayName("GET /users/{username}/roles")
  @Nested
  inner class GetRolesByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("ROLE_USER1")
          .firstName("ROLE")
          .lastName("USER1")
          .atPrisons(listOf("BXI", "WWI"))
          .dpsRoles(listOf("VIEW_PRISONER_DATA", "POM"))
          .nomisRoles(listOf("300", "200"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/users/ROLE_USER1/roles")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.get().uri("/users/ROLE_USER1/roles")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.get().uri("/users/ROLE_USER1/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user with roles not found`() {

      webTestClient.get().uri("/users/dummy/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `get user with DPS roles`() {

      webTestClient.get().uri("/users/ROLE_USER1/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "VIEW_PRISONER_DATA").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "POM").exists()
    }

    @Test
    fun `get user with NOMIS roles`() {

      webTestClient.get().uri("/users/ROLE_USER1/roles?include-nomis-roles=true")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "BXI", "200").exists()
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "WWI", "200").exists()
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "BXI", "300").exists()
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "WWI", "300").exists()
    }
  }

  @DisplayName("POST /users/{username}/roles/{roleCode}")
  @Nested
  inner class AddRolesByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("ROLE_USER1")
          .firstName("MARK")
          .lastName("BOWLAN")
          .atPrison("BXI")
          .dpsRoles(listOf("VIEW_PRISONER_DATA", "POM"))
          .nomisRoles(listOf("400", "200"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.post().uri("/users/ROLE_USER1/roles/GLOBAL_SEARCH")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.post().uri("/users/ROLE_USER1/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `add role to user forbidden with wrong role`() {

      webTestClient.post().uri("/users/ROLE_USER1/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `add roles user not found`() {

      webTestClient.post().uri("/users/dummy/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `add role to user`() {

      webTestClient.get().uri("/users/ROLE_USER1/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "POM").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "VIEW_PRISONER_DATA").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "GLOBAL_SEARCH").doesNotExist()

      webTestClient.post().uri("/users/ROLE_USER1/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "POM").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "VIEW_PRISONER_DATA").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "GLOBAL_SEARCH").exists()

      webTestClient.get().uri("/users/ROLE_USER1/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "POM").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "VIEW_PRISONER_DATA").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "GLOBAL_SEARCH").exists()
    }

    @Test
    fun `add non-existant role to user`() {
      webTestClient.post().uri("/users/ROLE_USER1/roles/XXX")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role not found: Role XXX not found")
    }

    @Test
    fun `add existing role to user`() {
      webTestClient.post().uri("/users/ROLE_USER1/roles/POM")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role already exists: Role POM is already assigned to this user")
    }

    @Test
    fun `add role to user that is not same type`() {
      webTestClient.post().uri("/users/ROLE_USER1/roles/200")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role assignment invalid: Roles of type INST cannot be assigned to caseloads of type APP")
    }

    @Test
    fun `add NOMIS role to user`() {
      webTestClient.get().uri("/users/ROLE_USER1/roles?include-nomis-roles=true")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "BXI", "300").doesNotExist()

      webTestClient.post().uri("/users/ROLE_USER1/roles/300?caseloadId=BXI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isCreated

      webTestClient.get().uri("/users/ROLE_USER1/roles?include-nomis-roles=true")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "BXI", "300").exists()
    }

    @Test
    fun `add NOMIS role to user of wrong type`() {
      webTestClient.post().uri("/users/ROLE_USER1/roles/GLOBAL_SEARCH?caseloadId=BXI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role assignment invalid: Roles of type APP cannot be assigned to caseloads of type INST")
    }

    @Test
    fun `add non existant NOMIS role to user`() {
      webTestClient.post().uri("/users/ROLE_USER1/roles/XXX?caseloadId=BXI")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role not found: Role XXX not found")
    }
  }

  @DisplayName("DELETE /users/{username}/roles/{roleCode}")
  @Nested
  inner class DeleteRoleByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("ROLE_USER1")
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
      webTestClient.delete().uri("/users/ROLE_USER1/roles/APPROVE_CATEGORISATION")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.delete().uri("/users/ROLE_USER1/roles/APPROVE_CATEGORISATION")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `delete role forbidden with wrong role`() {

      webTestClient.delete().uri("/users/ROLE_USER1/roles/APPROVE_CATEGORISATION")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `delete user role with username not found`() {

      webTestClient.delete().uri("/user/dummy/roles/APPROVE_CATEGORISATION")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `delete role from user`() {
      webTestClient.delete().uri("/users/ROLE_USER1/roles/APPROVE_CATEGORISATION")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("ROLE_USER1")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "CREATE_CATEGORISATION").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "GLOBAL_SEARCH").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "APPROVE_CATEGORISATION").doesNotExist()
    }

    @Test
    fun `delete non-existent role from user`() {
      webTestClient.delete().uri("/users/ROLE_USER1/roles/POM")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role not found: Role POM is not assigned to this user")
    }
  }
}
