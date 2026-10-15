# AGENTS.md

Guidance for AI coding agents (and humans) working in this repository.

## Project overview

NOMIS User Roles API — a self-contained Kotlin/Spring Boot fat-jar microservice
(part of the MoJ HMPPS digital prison services estate) used to manage users,
roles and caseloads in the NOMIS database.

## Tech stack

- JAVA 25
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

- Follow existing Kotlin style in the file you're editing. Although the HMPPS
  Gradle Spring Boot plugin includes ktlint, do not run any Gradle ktlint tasks
  in this repository; skip ktlint validation and formatting.
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

## Dependency injection

Prefer constructor injection over field injection in Spring components, including test classes.

- In production code, declare dependencies as constructor parameters (e.g. `private val fooRepository: FooRepository`) rather than using `@Autowired` on a field.
- In integration test classes, pass `@Autowired` repositories/beans as primary constructor parameters instead of using `@Autowired` on a `lateinit var` field.

## Tests

Prefer `@BeforeAll`/`@AfterAll` over `@BeforeEach`/`@AfterEach` where possible in nested integration test classes.

- Use `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` on the nested test class so that `@BeforeAll`/`@AfterAll` can be non-static (instance) methods.
- Build shared fixture data once in `@BeforeAll` and clean it up once in `@AfterAll`, rather than rebuilding it before every test, when the fixture is not mutated in a way that would leak between tests.
- When copying or adapting an integration test, preserve the full nested test structure, including `Security`, `Validation`, and `HappyPath` scenarios for each endpoint.
- Place each `@AfterAll` method immediately after the corresponding `@BeforeAll` setup method.
- OAuth2 propagation tests must verify the exact WireMock header value with `equalTo("Bearer ABCDE")`; do not weaken this assertion to a regex or non-empty-header matcher.

## Linting

Do not run `ktlintCheck` (or any ktlint gradle task) in this repository. Skip ktlint validation entirely.
