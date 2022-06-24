package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "reporting")
data class ReportingConfiguration(var client: Client = Client())

data class Client(val id: String = "", val secret: String = "")
