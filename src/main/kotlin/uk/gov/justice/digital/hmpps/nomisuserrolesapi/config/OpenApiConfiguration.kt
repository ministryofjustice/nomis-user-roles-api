package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version!!

  @Bean
  fun customOpenAPI(env: Environment): OpenAPI = OpenAPI()
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
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))

  @Bean
  fun openAPICustomiser(): OpenApiCustomizer = OpenApiCustomizer { }.also {
    PrimitiveType.enablePartialTime() // Prevents generation of a LocalTime schema which causes conflicts with java.time.LocalTime
  }
}
