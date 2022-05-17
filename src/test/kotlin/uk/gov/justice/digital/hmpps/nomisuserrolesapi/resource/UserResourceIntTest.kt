package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus

class UserResourceIntTest : IntegrationTestBase() {
  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @DisplayName("GET /users/{username}")
  @Nested
  inner class GetUserByUsername {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser().username("marco.rossi").firstName("Marco").lastName("Rossi").buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/users/marco.rossi")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user not found`() {

      webTestClient.get().uri("/users/dummy")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `get user with role ROLE_MAINTAIN_ACCESS_ROLES_ADMIN`() {

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("marco.rossi")
        .jsonPath("firstName").isEqualTo("Marco")
        .jsonPath("lastName").isEqualTo("Rossi")
        .jsonPath("staffId").exists()
    }

    @Test
    fun `get user with role ROLE_MAINTAIN_ACCESS_ROLES`() {

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("staffId").exists()
    }

    @Test
    fun `get user with role ROLE_MANAGE_NOMIS_USER_ACCOUNT`() {

      webTestClient.get().uri("/users/marco.rossi")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("staffId").exists()
    }
  }

  @DisplayName("GET /users/staff?firstName={firstName}&lastName={lastName}")
  @Nested
  inner class GetUserByFirstNameAndLastName {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser().username("marco.rossi")
          .firstName("Marco")
          .lastName("Rossi")
          .email("marco@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    private fun spec(surname: String = "Rossi") =
      webTestClient.get().uri {
        it.path("/users/staff")
          .queryParam("firstName", "Marco")
          .queryParam("lastName", surname).build()
      }

    @Test
    fun `access forbidden when no authority`() {
      spec().exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      spec().headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {
      spec().headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user by first name and last name not found`() {
      spec("Rossix").headers(setAuthorisation(roles = listOf("ROLE_STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("[]")
    }

    @Test
    fun `get user by first name and last name`() {
      spec().headers(setAuthorisation(roles = listOf("ROLE_STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] }).contains("marco.rossi")
        }
    }

    @Test
    fun `get user by first name and last name with use-of-force role`() {
      spec().headers(setAuthorisation(roles = listOf("ROLE_USE_OF_FORCE")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] }).contains("marco.rossi")
        }
    }

    @Test
    fun `get user returns email addresses too`() {
      spec().headers(setAuthorisation(roles = listOf("ROLE_STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["email"] }).contains("marco@justice.gov.uk")
        }
    }
  }

  @DisplayName("GET /users/user?email={emailAddress}")
  @Nested
  inner class GetUserByEmailAddress {
    private val matchByUserName = "$[?(@.username == '%s')]"

    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser().username("marco.rossi")
          .firstName("Marco")
          .lastName("Rossi")
          .email("marco@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
        generalUser().username("fred.smith")
          .firstName("Fred")
          .lastName("Smith")
          .email("fred@justice.gov.uk")
          .atPrison("MDI")
          .buildAndSave()
        generalUser().username("frederica.jones")
          .firstName("Frederica")
          .lastName("Jones")
          .email("fred@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.get().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.get().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user when email address not found`() {
      webTestClient.get().uri {
        it.path("/users/user")
          .queryParam("email", "missing@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("[]")
    }

    @Test
    fun `get user by email`() {
      webTestClient.get().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByUserName, "marco.rossi").exists()
    }

    @Test
    fun `get user by email with multiple matches`() {
      webTestClient.get().uri {
        it.path("/users/user")
          .queryParam("email", "fred@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").isArray
        .jsonPath(matchByUserName, "fred.smith").exists()
        .jsonPath(matchByUserName, "frederica.jones").exists()
    }
  }

  @DisplayName("POST /users/user?email={emailAddress}")
  @Nested
  inner class GetUsersByEmailAddressAndUsernames {
    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser().username("marco.rossi")
          .firstName("Marco")
          .lastName("Rossi")
          .email("marco@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
        generalUser().username("fred.smith")
          .firstName("Fred")
          .lastName("Smith")
          .email("fred@justice.gov.uk")
          .atPrison("MDI")
          .buildAndSave()
        generalUser().username("frederica.jones")
          .firstName("Frederica")
          .lastName("Jones")
          .email("fred@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user when email address not found`() {
      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "missing@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("[]")
    }

    @Test
    fun `get user when email address not found but match on username`() {
      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "missing@justice.gov.uk")
          .build()
      }
        .bodyValue(listOf("marco.rossi"))
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] })
            .containsExactly("marco.rossi")
            .hasSize(1)
        }
    }

    @Test
    fun `get user by email`() {
      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] }).contains("marco.rossi")
        }
    }

    @Test
    fun `get user by email remove duplicates`() {
      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "marco@justice.gov.uk").build()
      }
        .bodyValue(listOf("marco.rossi"))
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] })
            .contains("marco.rossi")
            .hasSize(1)
        }
    }

    @Test
    fun `get user by email with multiple matches`() {
      webTestClient.post().uri {
        it.path("/users/user")
          .queryParam("email", "fred@justice.gov.uk").build()
      }
        .bodyValue(listOf("marco.rossi"))
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] })
            .contains("fred.smith")
            .contains("frederica.jones")
            .contains("marco.rossi")
            .hasSize(3)
        }
    }
  }

  @DisplayName("GET /users/emails")
  @Nested
  inner class GetUsersAndEmails {

    @BeforeEach
    internal fun createUsers() {
      with(dataBuilder) {
        generalUser().username("marco.rossi")
          .firstName("Marco")
          .lastName("Rossi")
          .email("marco@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
        generalUser().username("fred.smith")
          .firstName("Fred")
          .lastName("Smith")
          .email("fred@justice.gov.uk")
          .atPrison("MDI")
          .buildAndSave()
        generalUser().username("frederica.jones")
          .firstName("Frederica")
          .lastName("Jones")
          .status("LOCKED")
          .email("frederica@justice.gov.uk")
          .atPrison("WWI")
          .buildAndSave()
      }
    }

    @AfterEach
    internal fun deleteUsers() = dataBuilder.deleteAllUsers()

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri {
        it.path("/users/emails").build()
      }
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {

      webTestClient.get().uri {
        it.path("/users/emails").build()
      }
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {

      webTestClient.get().uri {
        it.path("/users/emails").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get all usernames and their associated emails`() {
      webTestClient.get().uri {
        it.path("/users/emails").build()
      }
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGE_NOMIS_USER_ACCOUNT")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$").value<JSONArray> {
          assertThat(it.map { m -> (m as Map<*, *>)["username"] })
            .contains("fred.smith")
            .contains("marco.rossi")
            .contains("frederica.jones")
            .hasSize(3)
        }
    }
  }

  @DisplayName("GET /users/")
  @Nested
  inner class GetUser {
    val matchByUserName = "$.content[?(@.username == '%s')]"

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/users")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/users")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get user forbidden with wrong role`() {
      webTestClient.get().uri("/users")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Nested
    @DisplayName("when called by a national central administrator")
    inner class NationalCentralAdministrator {
      @BeforeEach
      internal fun createUsers() {
        with(dataBuilder) {
          generalUser()
            .username("abella.moulin")
            .firstName("ABELLA")
            .lastName("MOULIN")
            .atPrison("WWI")
            .dpsRoles(listOf("CREATE_CATEGORISATION", "GLOBAL_SEARCH"))
            .email("abella@justice.gov.uk")
            .buildAndSave()
          generalUser()
            .username("marco.rossi")
            .firstName("MARCO")
            .lastName("ROSSI")
            .atPrisons(listOf("WWI", "BXI")).inactive()
            .dpsRoles(listOf("APPROVE_CATEGORISATION", "GLOBAL_SEARCH"))
            .email("marco@justice.gov.uk")
            .buildAndSave()
          generalUser()
            .username("mark.bowlan")
            .firstName("MARK")
            .lastName("BOWLAN")
            .atPrison("BXI")
            .dpsRoles(listOf("APPROVE_CATEGORISATION", "CREATE_CATEGORISATION", "GLOBAL_SEARCH"))
            .email("mark@justice.gov.uk")
            .buildAndSave()
          generalUser()
            .username("ella.dribble")
            .firstName("ELLA")
            .lastName("DRIBBLE")
            .atPrisons(listOf("BXI", "WWI"))
            .inactive()
            .dpsRoles(listOf()).buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      fun `they can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES_ADMIN role`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(4)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }

      @Test
      internal fun `will return the count of DPS roles`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.totalElements").isEqualTo(4)
          .jsonPath(matchByUserName + "dpsRoleCount", "marco.rossi").isEqualTo(2)
          .jsonPath(matchByUserName + "dpsRoleCount", "abella.moulin").isEqualTo(2)
          .jsonPath(matchByUserName + "dpsRoleCount", "mark.bowlan").isEqualTo(3)
          .jsonPath(matchByUserName + "dpsRoleCount", "ella.dribble").isEqualTo(0)
      }

      @Test
      internal fun `will return the email addresss`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.totalElements").isEqualTo(4)
          .jsonPath(matchByUserName + "email", "marco.rossi").isEqualTo("marco@justice.gov.uk")
          .jsonPath(matchByUserName + "email", "abella.moulin").isEqualTo("abella@justice.gov.uk")
          .jsonPath(matchByUserName + "email", "mark.bowlan").isEqualTo("mark@justice.gov.uk")
          .jsonPath(matchByUserName + "email", "ella.dribble").isEqualTo(null)
      }

      @Test
      fun `blank filters are ignored`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("nameFilter", "")
            .queryParam("caseload", "")
            .queryParam("activeCaseload", "")
            .queryParam("accessRoles", "")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(4)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }

      @Test
      fun `they can filter by user name`() {
        webTestClient.get().uri { it.path("/users/").queryParam("nameFilter", "mar").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
      }

      @Test
      fun `they can filter by account status`() {
        webTestClient.get().uri { it.path("/users/").queryParam("status", "ACTIVE").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
      }

      @Test
      fun `invalid account status filter is a bad request`() {
        webTestClient.get().uri { it.path("/users/").queryParam("status", "INACT").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isBadRequest
      }

      @Test
      fun `they can filter by active caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("activeCaseload", "WWI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
      }

      @Test
      fun `they can filter by caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("caseload", "WWI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(3)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }

      @Test
      fun `they can filter by role`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("accessRoles", "CREATE_CATEGORISATION")
            .queryParam("accessRoles", "GLOBAL_SEARCH")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
      }
    }

    @Nested
    @DisplayName("when called by a local administrator")
    inner class LocalAdministrator {
      @BeforeEach
      internal fun createUsers() {
        with(dataBuilder) {
          localAdministrator().username("jane.lsa.wwi").atPrison("WWI").buildAndSave()

          generalUser().username("abella.moulin").firstName("ABELLA").lastName("MOULIN").atPrison("WWI").buildAndSave()
          // first prison is set to active caseload
          generalUser().username("marco.rossi").firstName("MARCO").lastName("ROSSI").atPrisons(listOf("BXI", "WWI"))
            .inactive().buildAndSave()
          generalUser().username("ella.dribble").firstName("ELLA").lastName("DRIBBLE").atPrisons(listOf("WWI", "BXI"))
            .inactive().buildAndSave()
          generalUser().username("mark.bowlan").firstName("MARK").lastName("BOWLAN").atPrison("BXI").buildAndSave()
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      fun `they can call the endpoint with the ROLE_MAINTAIN_ACCESS_ROLES role`() {
        webTestClient.get().uri("/users/")
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(3)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
          .jsonPath(matchByUserName + "active", "abella.moulin").isEqualTo(true)
          .jsonPath(matchByUserName + "firstName", "abella.moulin").isEqualTo("Abella")
          .jsonPath(matchByUserName + "lastName", "abella.moulin").isEqualTo("Moulin")
          .jsonPath(matchByUserName + "staffId", "abella.moulin").exists()
          .jsonPath(matchByUserName + "activeCaseload.id", "abella.moulin").isEqualTo("WWI")
          .jsonPath(matchByUserName + "activeCaseload.name", "abella.moulin").isEqualTo("Wandsworth (HMP)")
      }

      @Test
      internal fun `they would see all users, including themselves,  if they have the nation admin role`() {
        webTestClient.get().uri("/users/")
          .headers(
            setAuthorisation(
              roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
              user = "jane.lsa.wwi"
            )
          )
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(5)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "abella.moulin").exists()
          .jsonPath(matchByUserName, "mark.bowlan").exists()
          .jsonPath(matchByUserName, "jane.lsa.wwi").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }

      @Test
      fun `they can filter by user name`() {
        webTestClient.get().uri { it.path("/users/").queryParam("nameFilter", "mar").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(1)
          .jsonPath(matchByUserName, "marco.rossi").exists()
      }

      @Test
      fun `they can filter by account status`() {
        webTestClient.get().uri { it.path("/users/").queryParam("status", "ACTIVE").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(1)
          .jsonPath(matchByUserName, "abella.moulin").exists()
      }

      @Test
      fun `they can filter by active caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("activeCaseload", "BXI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(1)
          .jsonPath(matchByUserName, "marco.rossi").exists()
      }

      @Test
      fun `they can filter by caseload`() {
        webTestClient.get().uri { it.path("/users/").queryParam("caseload", "BXI").build() }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES"), user = "jane.lsa.wwi"))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(2)
          .jsonPath(matchByUserName, "marco.rossi").exists()
          .jsonPath(matchByUserName, "ella.dribble").exists()
      }
    }

    @Nested
    @DisplayName("sorting and paging")
    inner class SortingAndPaging {
      @BeforeEach
      internal fun setUp() {
        with(dataBuilder) {
          generalUser()
            .username("aabella.moulin")
            .firstName("AABELLA")
            .lastName("MOULIN")
            .atPrison("WWI")
            .buildAndSave()
          generalUser()
            .username("aaabella.moulin")
            .firstName("AAABELLA")
            .lastName("MOULIN")
            .atPrison("WWI")
            .buildAndSave()
          generalUser()
            .username("zabella.moulin")
            .firstName("ZAABELLA")
            .lastName("MOULIN")
            .atPrison("WWI")
            .buildAndSave()
          generalUser()
            .username("xaabella.aoulin")
            .firstName("XAABELLA")
            .lastName("AOULIN")
            .atPrison("WWI")
            .buildAndSave()
          generalUser()
            .username("xaabella.aaoulin")
            .firstName("XAABELLA")
            .lastName("AAOULIN")
            .atPrison("WWI")
            .buildAndSave()
          generalUser()
            .username("xaabella.zaoulin")
            .firstName("XAABELLA")
            .lastName("ZAOULIN")
            .atPrison("WWI")
            .buildAndSave()
          generalUser()
            .username("daabella.daoulin")
            .firstName("DAABELLA")
            .lastName("DAOULIN")
            .atPrison("MDI")
            .buildAndSave()
          generalUser()
            .username("faabella.daoulin")
            .firstName("FAABELLA")
            .lastName("DAOULIN")
            .atPrison("BXI")
            .buildAndSave()

          (1..101).forEach {
            generalUser()
              .username("another.user$it")
              .firstName("ANOTHER")
              .lastName("USER")
              .status(AccountStatus.EXPIRED.desc)
              .atPrison("BXI")
              .buildAndSave()
          }
        }
      }

      @AfterEach
      internal fun deleteUsers() = dataBuilder.deleteAllUsers()

      @Test
      internal fun `can order by first name, last name ascending`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("status", "ACTIVE")
            .queryParam("sort", "firstName,lastName")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(8)
          .jsonPath("$.content[0].username").isEqualTo("aaabella.moulin")
          .jsonPath("$.content[1].username").isEqualTo("aabella.moulin")
          .jsonPath("$.content[2].username").isEqualTo("daabella.daoulin")
          .jsonPath("$.content[3].username").isEqualTo("faabella.daoulin")
          .jsonPath("$.content[4].username").isEqualTo("xaabella.aaoulin")
          .jsonPath("$.content[5].username").isEqualTo("xaabella.aoulin")
          .jsonPath("$.content[6].username").isEqualTo("xaabella.zaoulin")
          .jsonPath("$.content[7].username").isEqualTo("zabella.moulin")
      }

      @Test
      internal fun `can order by last name, first name descending`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("status", "ACTIVE")
            .queryParam("sort", "lastName,firstName,desc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(8)
          .jsonPath("$.content[0].username").isEqualTo("xaabella.zaoulin")
          .jsonPath("$.content[1].username").isEqualTo("zabella.moulin")
          .jsonPath("$.content[2].username").isEqualTo("aabella.moulin")
          .jsonPath("$.content[3].username").isEqualTo("aaabella.moulin")
          .jsonPath("$.content[4].username").isEqualTo("faabella.daoulin")
          .jsonPath("$.content[5].username").isEqualTo("daabella.daoulin")
          .jsonPath("$.content[6].username").isEqualTo("xaabella.aoulin")
          .jsonPath("$.content[7].username").isEqualTo("xaabella.aaoulin")
      }

      @Test
      internal fun `default order is last name, first name ascending`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("status", "ACTIVE")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(8)
          .jsonPath("$.content[0].username").isEqualTo("xaabella.aaoulin")
          .jsonPath("$.content[1].username").isEqualTo("xaabella.aoulin")
          .jsonPath("$.content[2].username").isEqualTo("daabella.daoulin")
          .jsonPath("$.content[3].username").isEqualTo("faabella.daoulin")
          .jsonPath("$.content[4].username").isEqualTo("aaabella.moulin")
          .jsonPath("$.content[5].username").isEqualTo("aabella.moulin")
          .jsonPath("$.content[6].username").isEqualTo("zabella.moulin")
          .jsonPath("$.content[7].username").isEqualTo("xaabella.zaoulin")
      }

      @Test
      internal fun `can order by username`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("status", "ACTIVE")
            .queryParam("sort", "username,asc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(8)
          .jsonPath("$.content[0].username").isEqualTo("aaabella.moulin")
          .jsonPath("$.content[1].username").isEqualTo("aabella.moulin")
          .jsonPath("$.content[2].username").isEqualTo("daabella.daoulin")
          .jsonPath("$.content[3].username").isEqualTo("faabella.daoulin")
          .jsonPath("$.content[4].username").isEqualTo("xaabella.aaoulin")
          .jsonPath("$.content[5].username").isEqualTo("xaabella.aoulin")
          .jsonPath("$.content[6].username").isEqualTo("xaabella.zaoulin")
          .jsonPath("$.content[7].username").isEqualTo("zabella.moulin")
      }

      @Test
      internal fun `can order by activeCaseload`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("status", "ACTIVE")
            .queryParam("sort", "activeCaseload,username,asc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.numberOfElements").isEqualTo(8)
          .jsonPath("$.content[0].username").isEqualTo("faabella.daoulin")
          .jsonPath("$.content[1].username").isEqualTo("daabella.daoulin")
      }

      @Test
      internal fun `can order by account status`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("sort", "status,username,asc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content[0].username").isEqualTo("another.user1")
          .jsonPath("$.content[0].active").isEqualTo(false)

        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("sort", "status,username,desc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content[0].username").isEqualTo("zabella.moulin")
          .jsonPath("$.content[0].active").isEqualTo(true)
      }

      @Test
      internal fun `paging information is sent in the response`() {
        webTestClient.get().uri {
          it.path("/users/")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("totalElements").isEqualTo(109)
          .jsonPath("numberOfElements").isEqualTo(10)
          .jsonPath("number").isEqualTo(0)
          .jsonPath("totalPages").isEqualTo(11)
          .jsonPath("size").isEqualTo(10)
      }

      @Test
      internal fun `can request a different page size`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("size", "20")
            .queryParam("sort", "username,asc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("totalElements").isEqualTo(109)
          .jsonPath("numberOfElements").isEqualTo(20)
          .jsonPath("number").isEqualTo(0)
          .jsonPath("totalPages").isEqualTo(6)
          .jsonPath("size").isEqualTo(20)
          .jsonPath("$.content[0].username").isEqualTo("aaabella.moulin")
      }

      @Test
      internal fun `can request a different page`() {
        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("size", "20")
            .queryParam("page", "3")
            .queryParam("sort", "username,asc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("totalElements").isEqualTo(109)
          .jsonPath("numberOfElements").isEqualTo(20)
          .jsonPath("number").isEqualTo(3)
          .jsonPath("totalPages").isEqualTo(6)
          .jsonPath("size").isEqualTo(20)
          .jsonPath("$.content[0].username").isEqualTo("another.user60")
          .jsonPath("$.content[1].username").isEqualTo("another.user61")
      }
    }
  }

  @Nested
  @DisplayName("DELETE /users/{username}")
  inner class DeleteUsers {

    @Test
    fun `can't drop a db user that doesn't exist`() {
      webTestClient.delete().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `can't drop a db user without correct role`() {
      webTestClient.delete().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can drop a db user that does exist`() {
      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "testuser3",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.delete().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/users/TESTUSER3")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
