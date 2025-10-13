package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UpdateRoleRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class RoleResourceIntTest : IntegrationTestBase() {

  @DisplayName("GET /roles")
  @Nested
  inner class GetRoles {
    val matchByRoleCode = "$.[?(@.code == '%s')]"

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/roles")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `get roles forbidden with wrong role`() {
      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get roles is allowed for ROLE_MAINTAIN_ACCESS_ROLES_ADMIN and ROLE_MAINTAIN_ACCESS_ROLES and ROLE_VIEW_NOMIS_STAFF_DETAILS`() {
      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "GLOBAL_SEARCH").exists()
        .jsonPath(matchByRoleCode, "DELETE_SENSITIVE_CASE_NOTES").exists()

      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "GLOBAL_SEARCH").exists()
        .jsonPath(matchByRoleCode, "DELETE_SENSITIVE_CASE_NOTES").exists()

      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_NOMIS_STAFF_DETAILS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "GLOBAL_SEARCH").exists()
        .jsonPath(matchByRoleCode, "DELETE_SENSITIVE_CASE_NOTES").exists()
    }

    @Test
    internal fun `can opt to retrieve NOMIS role`() {
      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "300").doesNotExist()

      webTestClient.get().uri { it.path("/roles").queryParam("all-roles", "true").build() }
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "300").exists()
    }

    @Test
    internal fun `by default all DPS roles, including admin roles, will be retrieved`() {
      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "GLOBAL_SEARCH").exists()
        .jsonPath(matchByRoleCode, "MOIC_ADMIN").exists()
    }

    @Test
    internal fun `can opt to only return general DPS roles`() {
      webTestClient.get().uri { it.path("/roles").queryParam("admin-roles", "false").build() }
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByRoleCode, "GLOBAL_SEARCH").exists()
        .jsonPath(matchByRoleCode, "MOIC_ADMIN").doesNotExist()
    }
  }

  @DisplayName("GET /roles/{code}")
  @Nested
  inner class GetRolesByCode {

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/roles/GLOBAL_SEARCH")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get role forbidden with wrong role`() {
      webTestClient.get().uri("/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get role not found`() {
      webTestClient.get().uri("/roles/dummy")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `get role`() {
      webTestClient.get().uri("/roles/GLOBAL_SEARCH")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("code").isEqualTo("GLOBAL_SEARCH")
        .jsonPath("name").isEqualTo("Global Search")
        .jsonPath("sequence").isEqualTo("1")
        .jsonPath("type").isEqualTo("APP")
        .jsonPath("adminRoleOnly").isEqualTo("false")
    }
  }

  @Nested
  @DisplayName("POST /roles")
  inner class CreateRoles {

    @Test
    fun `a role cannot be created without correct role`() {
      webTestClient.post().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(
          BodyInserters.fromValue(
            CreateRoleRequest(
              code = "TEST_ROLE1",
              name = "A Test role",
            ),
          ),
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `a role can be created`() {
      webTestClient.post().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            CreateRoleRequest(
              code = "test_role1",
              name = "A Test role",
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
                             {
                                "code": "TEST_ROLE1",
                                "name": "A Test role",
                                "sequence": 1,
                                "type": "APP",
                                "adminRoleOnly": false
                            }
                            """,
        )
    }

    @Test
    fun `a sub role cannot be created if parent does not exist`() {
      webTestClient.post().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            CreateRoleRequest(
              code = "TEST_ROLE1",
              name = "A Test role",
              parentRoleCode = "DUMMY",
            ),
          ),
        )
        .exchange()
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Role not found: Parent role with code DUMMY not found")
    }

    @Test
    fun `a sub role can be created`() {
      webTestClient.post().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            CreateRoleRequest(
              code = "TEST_ROLE2",
              name = "A Test sub role",
              parentRoleCode = "LICENCE_ROLE",
              adminRoleOnly = true,
            ),
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
                             {
                                "code": "TEST_ROLE2",
                                "name": "A Test sub role",
                                "sequence": 1,
                                "type": "APP",
                                "adminRoleOnly": true,
                                "parentRole" : {
                                    "code": "LICENCE_ROLE",
                                    "name": "Licence roles (Not used)",
                                    "sequence": 1,
                                    "type": "APP",
                                    "adminRoleOnly": false
                                }
                            }
                            """,
        )
    }
  }

  @Nested
  @DisplayName("PUT /roles/{code}")
  inner class UpdateRoles {

    @Test
    fun `a role cannot be updated without correct role`() {
      webTestClient.put().uri("/roles/LICENCE_RO")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(
          BodyInserters.fromValue(
            UpdateRoleRequest(
              name = "Updated Role Name",
            ),
          ),
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `a role name can be updated`() {
      webTestClient.put().uri("/roles/LICENCE_RO")
        .headers(setAuthorisation(roles = listOf("ROLE_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            UpdateRoleRequest(
              name = "Updated Role Name",
            ),
          ),
        )
        .exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
                             {
                                "code": "LICENCE_RO",
                                "name": "Updated Role Name",
                                "type": "APP",
                                "adminRoleOnly": true
                            }
                            """,
        )
    }

    @Test
    fun `a role adminRoleOnly can be updated`() {
      webTestClient.put().uri("/roles/LICENCE_DM")
        .headers(setAuthorisation(roles = listOf("ROLE_ROLES_ADMIN")))
        .body(
          BodyInserters.fromValue(
            UpdateRoleRequest(
              adminRoleOnly = true,
            ),
          ),
        )
        .exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
                             {
                                "code": "LICENCE_DM",
                                "name": "Licence Decision Maker",
                                "type": "APP",
                                "adminRoleOnly": true
                            }
                            """,
        )
    }
  }

  @Nested
  @DisplayName("DELETE /roles/{code}")
  inner class DeleteRoles {

    @Test
    fun `a role cannot be deleted without correct role`() {
      webTestClient.delete().uri("/roles/CATEGORISATION_READONLY")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `a role cannot be deleted if not exists`() {
      webTestClient.delete().uri("/roles/DUMMY")
        .headers(setAuthorisation(roles = listOf("ROLE_DELETE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `a role can be deleted`() {
      webTestClient.delete().uri("/roles/CATEGORISATION_READONLY")
        .headers(setAuthorisation(roles = listOf("ROLE_DELETE_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/roles/CATEGORISATION_READONLY")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
