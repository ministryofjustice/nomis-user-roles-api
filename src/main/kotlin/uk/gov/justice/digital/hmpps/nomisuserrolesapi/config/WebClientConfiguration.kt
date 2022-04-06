package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.reporting}") val reportingBaseUri: String
) {

  @Bean("reportingApiWebClient")
  fun reportingApiWebClient(): WebClient {
    return WebClient.builder()
      .baseUrl(reportingBaseUri)
      .build()
  }
}
