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

class MeResourceIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @DisplayName("GET /me")
  @Nested
  inner class GetUserByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser()
          .username("AUTH_ADM")
          .firstName("AUTH")
          .lastName("ADM")
          .atPrisons(listOf("BXI", "WWI"))
          .dpsRoles(listOf("CREATE_CATEGORISATION", "GLOBAL_SEARCH"))
          .nomisRoles(listOf("300", "200"))
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()


    @Test
    fun `get user`() {

      webTestClient.get().uri("/me")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("AUTH_ADM")
        .jsonPath("firstName").isEqualTo("AUTH")
        .jsonPath("lastName").isEqualTo("ADM")
        .jsonPath("staffId").exists()
    }

    @Test
    fun `get user in context not found`() {

      webTestClient.get().uri("/me")
        .headers(setAuthorisation(user = "dummy"))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `get user with DPS roles`() {

      webTestClient.get().uri("/me/roles")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("AUTH_ADM")
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "CREATE_CATEGORISATION").exists()
        .jsonPath("$.dpsRoles[?(@.code == '%s')]", "GLOBAL_SEARCH").exists()
    }

    @Test
    fun `get user roles in context not found`() {

      webTestClient.get().uri("/me/roles")
        .headers(setAuthorisation(user = "dummy"))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `get user with NOMIS roles`() {

      webTestClient.get().uri("/me/roles?include-nomis-roles=true")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("AUTH_ADM")
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "BXI", "200").exists()
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "WWI", "200").exists()
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "BXI", "300").exists()
        .jsonPath("$.nomisRoles[?(@.caseload.id == '%s')].roles[?(@.code == '%s')]", "WWI", "300").exists()
    }

    @Test
    fun `get user with caseloads`() {

      webTestClient.get().uri("/me/caseloads")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("AUTH_ADM")
        .jsonPath("activeCaseload.id").isEqualTo("BXI")
        .jsonPath("$.caseloads[?(@.id == '%s')]", "BXI").exists()
        .jsonPath("$.caseloads[?(@.id == '%s')]", "WWI").exists()
    }


    @Test
    fun `get user caseloads in context not found`() {

      webTestClient.get().uri("/me/caseloads")
        .headers(setAuthorisation(user = "dummy"))
        .exchange()
        .expectStatus().isNotFound
    }
  }

}
