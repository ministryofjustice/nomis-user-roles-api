plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.2.0-beta-2"
  kotlin("plugin.spring") version "1.8.21"
  kotlin("plugin.jpa") version "1.8.21"
  idea
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("commons-codec:commons-codec:1.15")

  implementation("org.flywaydb:flyway-core")
  implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.4.1")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.xml.bind:jaxb-api:2.3.1")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("com.zaxxer:HikariCP:5.0.1")
  runtimeOnly("com.h2database:h2:2.1.214")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.19.0.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
  testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.26.0")
  testImplementation("javax.xml.bind:jaxb-api:2.3.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }
  }
}
