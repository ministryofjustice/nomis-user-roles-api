package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class RoleResourceIntTest : IntegrationTestBase() {

  @DisplayName("GET /roles")
  @Nested
  inner class GetRoles {

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
        .jsonPath("$.[?(@.code == 'GLOBAL_SEARCH')]").exists()
        .jsonPath("$.[?(@.code == 'DELETE_SENSITIVE_CASE_NOTES')]").exists()

      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_NOMIS_STAFF_DETAILS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$.[?(@.code == 'GLOBAL_SEARCH')]").exists()
        .jsonPath("$.[?(@.code == 'DELETE_SENSITIVE_CASE_NOTES')]").exists()
    }

    @Test
    internal fun `can opt to retrieve NOMIS role`() {
      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$.[?(@.code == '300')]").doesNotExist()

      webTestClient.get().uri { it.path("/roles").queryParam("all-roles", "true").build() }
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$.[?(@.code == '300')]").exists()
    }

    @Test
    internal fun `by default all DPS roles, including admin roles, will be retrieved`() {
      webTestClient.get().uri("/roles")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$.[?(@.code == 'GLOBAL_SEARCH')]").exists()
        .jsonPath("$.[?(@.code == 'MOIC_ADMIN')]").exists()
    }

    @Test
    internal fun `can opt to only return general DPS roles`() {
      webTestClient.get().uri { it.path("/roles").queryParam("admin-roles", "false").build() }
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath("$.[?(@.code == 'GLOBAL_SEARCH')]").exists()
        .jsonPath("$.[?(@.code == 'MOIC_ADMIN')]").doesNotExist()
    }
  }
}
