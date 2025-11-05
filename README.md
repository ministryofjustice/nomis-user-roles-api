# NOMIS User Roles API


[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fnomis-user-roles-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/nomis-user-roles-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/nomis-user-roles-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://nomis-user-roles-api-dev.prison.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

Self-contained fat-jar micro-service to interact with users in the NOMIS database.

## Building

```./gradlew build```

## Running

### Local

Use the "dev" Spring profile.  Pass this profile to your IntelliJ "[Run Configuration](https://www.jetbrains.com/help/idea/run-debug-configuration-spring-boot.html)" or [the command line](https://docs.spring.io/spring-boot/gradle-plugin/running.html#running-your-application.passing-arguments) in the usual way.

Once up the application should be available on port 8082 - see the health page at http://localhost:8082/health.

Also try http://localhost:8082/swagger-ui.html to see the API specification.

Set `spring.h2.console.enabled` to true in `application-dev.yml`. It is set to false in the repo because leaving it as true creates a false positive in the veracode scan.
