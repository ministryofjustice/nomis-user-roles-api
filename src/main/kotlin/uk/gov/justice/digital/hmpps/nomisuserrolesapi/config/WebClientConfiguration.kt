package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.utils.UserContext
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.oauth}") val authbaseUri: String,
  @Value("\${api.base.timeout.oauth:2s}") val timeout: Duration
) {

  @Bean
  fun webClient(builder: WebClient.Builder): WebClient {

    return builder
      .baseUrl(authbaseUri)
      .clientConnector(getClientConnectorWithTimeouts(timeout, timeout))
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  private fun getClientConnectorWithTimeouts(
    connectTimeout: Duration,
    readTimeout: Duration
  ): ClientHttpConnector {
    val httpClient = HttpClient.create()
    return ReactorClientHttpConnector(
      httpClient
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout.toMillis().toInt())
        .doOnConnected { connection: Connection ->
          connection
            .addHandlerLast(ReadTimeoutHandler(readTimeout.toSeconds().toInt()))
        }
    )
  }

  private fun addAuthHeaderFilterFunction(): ExchangeFilterFunction {
    return ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
      val filtered = ClientRequest.from(request)
        .header(HttpHeaders.AUTHORIZATION, UserContext.getAuthToken())
        .build()
      next.exchange(filtered)
    }
  }
}
