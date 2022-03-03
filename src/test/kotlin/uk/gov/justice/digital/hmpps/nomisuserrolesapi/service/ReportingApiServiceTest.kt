package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.SpringAPIServiceTest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.wiremock.ReportingAuthenticationApiExtension.Companion.reportingAuthenticationApi

@SpringAPIServiceTest
@Import(ReportingApiService::class)
internal class ReportingApiServiceTest {
  @Autowired
  private lateinit var service: ReportingApiService

  @Nested
  inner class GetReportingUrl {
    @BeforeEach
    internal fun setUp() {
      reportingAuthenticationApi.stubSuccessfulAuthentication()
    }

    @Test
    internal fun `will send the client details to API`() {
      service.getReportingUrl("username")

      reportingAuthenticationApi.verifyAuthenticationRequestCredentials(
        // see application-test.yml for the expected values
        clientId = "reporting-client",
        clientSecret = "reporting-secret"
      )
    }

    @Test
    internal fun `will send the username to API`() {
      service.getReportingUrl("bobby.beans")

      reportingAuthenticationApi.verifyAuthenticationRequest("bobby.beans")
    }

    @Test
    internal fun `will return reporting url when successful`() {
      reportingAuthenticationApi.stubSuccessfulAuthentication("http://reporting-url")

      val response = service.getReportingUrl("username")

      assertThat(response.url).isEqualTo("http://reporting-url")
    }

    @Nested
    inner class OnErrors {
      @Test
      internal fun `401 error is thrown when there is a 401 error`() {
        reportingAuthenticationApi.stubError(401)

        assertThatThrownBy {
          service.getReportingUrl("username")
        }.isInstanceOf(WebClientResponseException.Unauthorized::class.java)
      }

      @Test
      internal fun `403 error is thrown when there is a 403 error`() {
        reportingAuthenticationApi.stubError(403)

        assertThatThrownBy {
          service.getReportingUrl("username")
        }.isInstanceOf(WebClientResponseException.Forbidden::class.java)
      }

      @Test
      internal fun `503 error is thrown when there is a 404 error`() {
        reportingAuthenticationApi.stubError(404)

        assertThatThrownBy {
          service.getReportingUrl("username")
        }.isInstanceOf(WebClientResponseException.ServiceUnavailable::class.java)
      }

      @Test
      internal fun `503 error is thrown when there is a 503 error`() {
        reportingAuthenticationApi.stubError(503)

        assertThatThrownBy {
          service.getReportingUrl("username")
        }.isInstanceOf(WebClientResponseException.ServiceUnavailable::class.java)
      }

      @Test
      internal fun `503 error is thrown when there is a 500 error`() {
        reportingAuthenticationApi.stubError(500)

        assertThatThrownBy {
          service.getReportingUrl("username")
        }.isInstanceOf(WebClientResponseException.ServiceUnavailable::class.java)
      }
    }
  }
}
