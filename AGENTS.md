# AGENTS.md

Guidance for AI coding agents (and humans) working in this repository.

## Project overview

NOMIS User Roles API — a self-contained Kotlin/Spring Boot fat-jar microservice
(part of the MoJ HMPPS digital prison services estate) used to manage users,
roles and caseloads in the NOMIS database.

## Tech stack

- Language: Kotlin, JVM toolchain 25 (see `.java-version`)
- Framework: Spring Boot (webclient, data-jpa, jdbc, oauth2-resource-server, flyway)
- Build: Gradle (Kotlin DSL) via the wrapper — always use `./gradlew`, never a
  globally installed `gradle`
- Base plugin: `uk.gov.justice.hmpps.gradle-spring-boot` (HMPPS shared Spring Boot conventions)
- Database: H2 at runtime for tests/dev, Oracle driver for real NOMIS DB; Flyway migrations
- API docs: springdoc-openapi (Swagger UI)

## Repository layout

- `src/main/kotlin/uk/gov/justice/digital/hmpps/nomisuserrolesapi/`
  - `config/` — Spring configuration (security, web, OpenAPI, etc.)
  - `data/` — request/response DTOs
  - `db/` — Flyway migrations and DB-related helpers
  - `health/` — health check indicators
  - `jpa/` — JPA entities and repositories
  - `resource/` — REST controllers
  - `service/` — business logic/services
  - `utils/` — shared utilities
- `src/test/kotlin/...` — unit and integration tests mirroring the main package structure
- `helm_deploy/` — Helm chart and per-environment values for Kubernetes deployment
- `scripts/` — operational scripts for bulk role/caseload changes
- `doc/architecture/` — architecture decision records / diagrams
- `Dockerfile`, `docker-compose.yml` — container build and local compose setup

## Build, test and run

- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run locally: start with the `dev` Spring profile (IntelliJ run config or
  `--args='--spring.profiles.active=dev'`); app serves on port 8082
  - Health check: http://localhost:8082/health
  - Swagger UI: http://localhost:8082/swagger-ui.html

Always run the smallest relevant Gradle task(s) after making changes
(e.g. `./gradlew test` or a specific test class) before considering a change complete.

## Conventions

- Follow existing Kotlin style in the file you're editing; this repo uses the
  conventions enforced by the HMPPS Gradle Spring Boot plugin (includes ktlint).
- Keep DTOs in `data/`, entities in `jpa/`, controllers in `resource/`, and
  business logic in `service/` — don't mix layers.
- Add Flyway migrations for any schema change under the existing `db/` migration path;
  never edit a previously-released migration.
- Mirror new source files with corresponding tests under `src/test/kotlin/...`.
- Don't enable `spring.h2.console.enabled` in committed config (causes a veracode scan false positive).

## Security / sensitive files

- Do not commit secrets, credentials, or real NOMIS data.
- `.snyk` and `dps-gradle-spring-boot-suppressions.xml` manage known vulnerability
  suppressions — update thoughtfully and only with justification.
