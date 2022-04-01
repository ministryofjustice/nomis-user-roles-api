package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ReportingConfiguration
import java.nio.charset.Charset

@Service
@EnableConfigurationProperties(ReportingConfiguration::class)
class ReportingApiService(
  @Qualifier("reportingApiWebClient") private val webClient: WebClient,
  private val config: ReportingConfiguration
) {
  fun getReportingUrl(username: String): ReportingUrlResponse =
    webClient.post()
      .uri("/bipssorws/sso/logon/$username")
      .header(ACCEPT, "application/json")
      .header("X-NOMIS-REP-CLIENTID", config.client.id)
      .header("X-NOMIS-REP-APIKEY", config.client.secret)
      .retrieve()
      .bodyToMono(ReportingUrlResponse::class.java)
      .onErrorMap(WebClientResponseException.NotFound::class.java) { it.mapTo(503) }
      .onErrorMap(WebClientResponseException.InternalServerError::class.java) { it.mapTo(503) }
      .block()!!
}

data class ReportingUrlResponse(val fullURLEncodedLogonToken: String)

fun WebClientResponseException.mapTo(status: Int): WebClientResponseException = WebClientResponseException.create(
  status,
  this.statusText,
  this.headers,
  this.responseBodyAsByteArray,
  Charset.defaultCharset(),
  this.request
)
