package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://nomis-user.roles-api.prison.service.justice.gov.uk").description("Prod"),
        Server().url("https://nomis-user-roles-api-preprod.prison.service.justice.gov.uk").description("PreProd"),
        Server().url("https://nomis-user-roles-api-dev.prison.service.justice.gov.uk")
          .description("Development"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .info(
      Info().title("NOMIS User and Role Management API")
        .version(version)
        .description("Register providing NOMIS User and role information")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
}
