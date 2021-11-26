package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

class ReferenceDataResourceIntTest : IntegrationTestBase() {
  @DisplayName("GET /reference-data/caseloads")
  @Nested
  inner class GetCaseloads {
    private val spec = webTestClient.get().uri("/reference-data/caseloads")

    @Test
    fun `access forbidden when no authority`() {
      spec.exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access allowed even when no roles specified`() {
      spec.headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `access allowed for any role specified`() {
      spec.headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `will return all active general caseloads ordered by name`() {
      spec.headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").value(greaterThan(158))
        .jsonPath("$[?(@.id == 'AKI')]").exists()
        .jsonPath("$[0].name").isEqualTo("Acklington (HMP)")
        .jsonPath("$[0].id").isEqualTo("AKI")
        // central admin and DPS is filtered out
        .jsonPath("$[?(@.id == 'CADM_I')]").doesNotExist()
        .jsonPath("$[?(@.id == 'NWEB')]").doesNotExist()
    }
  }

  @DisplayName("GET /reference-data/email-domains")
  @Nested
  inner class GetActiveEmailDomains {
    private val spec = webTestClient.get().uri("/reference-data/email-domains")

    @Test
    fun `access forbidden when no authority`() {
      spec.exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access allowed even when no roles specified`() {
      spec.headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `access allowed for any role specified`() {
      spec.headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `will return all active email domains`() {
      spec.headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> m as Map<*, *> }).contains(
            mapOf(
              "code" to "CJSM",
              "domain" to "%.cjsm.net",
            )
          ).hasSizeGreaterThan(5)
        }
    }
  }
}
