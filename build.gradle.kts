plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.7.0"
  kotlin("plugin.spring") version "1.9.20"
  kotlin("plugin.jpa") version "1.9.20"
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
  implementation("org.apache.commons:commons-lang3:3.13.0")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("commons-codec:commons-codec:1.16.0")

  implementation("org.flywaydb:flyway-core")
  implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.6.0")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  implementation("javax.transaction:javax.transaction-api:1.3")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.google.guava:guava:32.1.3-jre")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

  implementation("com.pauldijou:jwt-core_2.11:5.0.0")

  implementation("com.zaxxer:HikariCP:5.0.1")
  runtimeOnly("com.h2database:h2:2.2.224")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.20.0.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
  testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  testImplementation("org.wiremock:wiremock:3.2.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")
  testImplementation("javax.xml.bind:jaxb-api:2.3.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}
