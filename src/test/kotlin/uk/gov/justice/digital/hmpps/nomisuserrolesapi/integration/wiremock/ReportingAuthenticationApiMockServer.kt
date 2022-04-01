package uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpStatus

class ReportingAuthenticationApiExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val reportingAuthenticationApi = ReportingAuthenticationApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    reportingAuthenticationApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    reportingAuthenticationApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    reportingAuthenticationApi.stop()
  }
}

class ReportingAuthenticationApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
  }

  fun stubSuccessfulAuthentication(reportUrl: String = "https://reporting.justice.gov.uk/nomis?session=12345") {
    stubFor(
      post(urlMatching("/bipssorws/sso/logon/.*")).willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(HttpStatus.OK.value())
          .withBody("""{"fullURLEncodedLogonToken": "$reportUrl"}""")
      )
    )
  }

  fun stubError(status: Int = HttpStatus.SERVICE_UNAVAILABLE.value()) {
    stubFor(
      post(urlMatching("/bipssorws/sso/logon/.*")).willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(status)
      )
    )
  }

  fun verifyAuthenticationRequest(username: String) {
    verify(
      postRequestedFor(urlEqualTo("/bipssorws/sso/logon/$username"))
    )
  }

  fun verifyAuthenticationRequestCredentials(clientId: String, clientSecret: String) {
    verify(
      postRequestedFor(urlMatching("/bipssorws/sso/logon/.*"))
        .withHeader("X-NOMIS-REP-CLIENTID", equalTo(clientId))
        .withHeader("X-NOMIS-REP-APIKEY", equalTo(clientSecret))
    )
  }
}
