package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.wiremock.ReportingAuthenticationApiExtension
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.wiremock.ReportingAuthenticationApiExtension.Companion.reportingAuthenticationApi
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseload

@ExtendWith(ReportingAuthenticationApiExtension::class)
class AuthoriseIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @Nested
  @DisplayName("POST /authorise/reporting")
  inner class Reporting {
    @Test
    internal fun `can not authorise when token is not present`() {
      webTestClient.post().uri("/authorise/reporting")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Nested
    @DisplayName("when the reporting service is not available")
    inner class ServiceNotAvailable {
      @BeforeEach
      internal fun setUp() {
        reportingAuthenticationApi.stubError(status = SERVICE_UNAVAILABLE.value())
        with(dataBuilder) {
          generalUser()
            .username("marco.rossi")
            .atPrison("WWI")
            .nomisRoles(listOf("970"))
            .buildAndSave()
        }
      }
      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      internal fun `will return service unavailable response`() {
        webTestClient.post().uri("/authorise/reporting")
          .headers(setAuthorisation(user = "marco.rossi"))
          .exchange()
          .expectStatus().isEqualTo(SERVICE_UNAVAILABLE)
      }

      @Test
      internal fun `will return service unavailable for any error`() {
        reportingAuthenticationApi.stubError(status = INTERNAL_SERVER_ERROR.value())
        webTestClient.post().uri("/authorise/reporting")
          .headers(setAuthorisation(user = "marco.rossi"))
          .exchange()
          .expectStatus().isEqualTo(SERVICE_UNAVAILABLE)
      }
    }

    @Nested
    @DisplayName("when the reporting service is available")
    inner class ServiceIsAvailable {
      @BeforeEach
      internal fun setUp() {
        reportingAuthenticationApi.stubSuccessfulAuthentication("https://reporting.justice.gov.uk/nomis?session=12345")
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Nested
      @DisplayName("when user has 970 (Business Objects Power User) role on active caseload")
      inner class BusinessObjectsPowerUserActiveCaseload {
        @BeforeEach
        internal fun createUsers() {
          with(dataBuilder) {
            generalUser()
              .username("marco.rossi")
              .atPrison("WWI")
              .nomisRoles(listOf("970"))
              .buildAndSave()
          }
        }

        @Test
        internal fun `will pass username to reporting service`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)

          reportingAuthenticationApi.verifyAuthenticationRequest(username = "marco.rossi")
        }

        @Test
        internal fun `will retrieve a report url`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)
            .expectBody()
            .jsonPath("$.reportingUrl").isEqualTo("https://reporting.justice.gov.uk/nomis?session=12345")
        }
      }

      @Nested
      @DisplayName("when user has 970 (Business Objects Power User) role on non active caseload")
      inner class BusinessObjectsPowerUserNonActiveCaseload {
        @BeforeEach
        internal fun createUsers() {
          with(dataBuilder) {
            generalUser()
              .username("marco.rossi")
              .atPrisons("WWI", "BXI")
              .nomisRoles("200", "970")
              .build()
              .apply {
                this.userPersonDetail.caseloads.removeRole(role = "970", prison = "WWI")
                this.save()
              }
          }
        }

        @Test
        internal fun `will pass username to reporting service`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)

          reportingAuthenticationApi.verifyAuthenticationRequest(username = "marco.rossi")
        }

        @Test
        internal fun `will retrieve a report url`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)
            .expectBody()
            .jsonPath("$.reportingUrl").isEqualTo("https://reporting.justice.gov.uk/nomis?session=12345")
        }
      }

      @Nested
      @DisplayName("when user has 980 (Business Objects Interactive User) role on active caseload")
      inner class BusinessObjectsInteractiveUserActiveCaseload {
        @BeforeEach
        internal fun createUsers() {
          with(dataBuilder) {
            generalUser()
              .username("marco.rossi")
              .atPrison("WWI")
              .nomisRoles(listOf("980"))
              .buildAndSave()
          }
        }

        @Test
        internal fun `will pass username to reporting service`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)

          reportingAuthenticationApi.verifyAuthenticationRequest(username = "marco.rossi")
        }

        @Test
        internal fun `will retrieve a report url`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)
            .expectBody()
            .jsonPath("$.reportingUrl").isEqualTo("https://reporting.justice.gov.uk/nomis?session=12345")
        }
      }

      @Nested
      @DisplayName("when user has 980 (Business Objects Interactive User) role on non active caseload")
      inner class BusinessObjectsInteractiveUserNonActiveCaseload {
        @BeforeEach
        internal fun createUsers() {
          with(dataBuilder) {
            generalUser()
              .username("marco.rossi")
              .atPrisons("WWI", "BXI")
              .nomisRoles("200", "980")
              .build()
              .apply {
                this.userPersonDetail.caseloads.removeRole(role = "980", prison = "WWI")
                this.save()
              }
          }
        }

        @Test
        internal fun `will pass username to reporting service`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)

          reportingAuthenticationApi.verifyAuthenticationRequest(username = "marco.rossi")
        }

        @Test
        internal fun `will retrieve a report url`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(OK)
            .expectBody()
            .jsonPath("$.reportingUrl").isEqualTo("https://reporting.justice.gov.uk/nomis?session=12345")
        }
      }

      @Nested
      @DisplayName("when user neither has 980 or 970 role on active caseload")
      inner class NoBusinessObjectsRoles {
        @BeforeEach
        internal fun createUsers() {
          with(dataBuilder) {
            generalUser()
              .username("marco.rossi")
              .atPrison("WWI")
              .nomisRoles(listOf("200"))
              .buildAndSave()
          }
        }

        @Test
        internal fun `access will be forbidden`() {
          webTestClient.post().uri("/authorise/reporting")
            .headers(setAuthorisation(user = "marco.rossi"))
            .exchange()
            .expectStatus().isEqualTo(FORBIDDEN)
        }
      }
    }
  }
}

private fun MutableList<UserCaseload>.removeRole(role: String, prison: String) {
  val caseload = this.find { it.caseload.id == prison }
  caseload?.let {
    it.roles.removeIf { caseloadRole -> caseloadRole.role.code == role }
  }
}
