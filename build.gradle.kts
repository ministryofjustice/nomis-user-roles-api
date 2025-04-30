import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.1.0"
  kotlin("plugin.spring") version "2.1.20"
  kotlin("plugin.jpa") version "2.1.20"
  idea
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
  implementation("org.apache.commons:commons-lang3:3.17.0")
  implementation("org.apache.commons:commons-text:1.13.1")
  implementation("commons-codec:commons-codec:1.18.0")
  implementation("com.google.guava:guava:33.4.8-jre")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("io.hypersistence:hypersistence-utils-hibernate-60:3.9.4")
  runtimeOnly("org.hibernate.orm:hibernate-community-dialects")
  runtimeOnly("com.h2database:h2:2.3.232")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.26.0.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
  testImplementation("org.wiremock:wiremock-standalone:3.13.0")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")

  testRuntimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
  }
}
