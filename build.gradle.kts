plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.3.4-beta"
  kotlin("plugin.spring") version "1.7.10"
  kotlin("plugin.jpa") version "1.7.10"
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
  implementation("org.apache.commons:commons-text:1.9")
  implementation("commons-codec:commons-codec:1.15")

  implementation("org.flywaydb:flyway-core:8.5.13")
  implementation("com.vladmihalcea:hibernate-types-52:2.16.2")

  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.xml.bind:jaxb-api:2.3.1")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.9")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.9")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.9")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("com.zaxxer:HikariCP:5.0.1")
  runtimeOnly("com.h2database:h2:2.1.214")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.15.0.0.1")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.mockito:mockito-inline:4.6.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "18"
    }
  }
}
