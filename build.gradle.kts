import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.1.1"
  kotlin("plugin.spring") version "2.2.20"
  kotlin("plugin.jpa") version "2.2.20"
  idea
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
  implementation("org.apache.commons:commons-lang3:3.19.0")
  implementation("org.apache.commons:commons-text:1.14.0")
  implementation("commons-codec:commons-codec:1.19.0")
  implementation("com.google.guava:guava:33.5.0-jre")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("io.hypersistence:hypersistence-utils-hibernate-60:3.9.4")
  runtimeOnly("org.hibernate.orm:hibernate-community-dialects")
  runtimeOnly("com.h2database:h2:2.4.240")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.28.0.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")

  testRuntimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
