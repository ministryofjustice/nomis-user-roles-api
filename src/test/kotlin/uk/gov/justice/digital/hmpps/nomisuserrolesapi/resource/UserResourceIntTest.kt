package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedAdminUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CreateLinkedGeneralUserRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase

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
    fun `get user`() {

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
            .buildAndSave()
          generalUser()
            .username("marco.rossi")
            .firstName("MARCO")
            .lastName("ROSSI")
            .atPrisons(listOf("WWI", "BXI")).inactive()
            .dpsRoles(listOf("APPROVE_CATEGORISATION", "GLOBAL_SEARCH"))
            .buildAndSave()
          generalUser()
            .username("mark.bowlan")
            .firstName("MARK")
            .lastName("BOWLAN")
            .atPrison("BXI")
            .dpsRoles(listOf("APPROVE_CATEGORISATION", "CREATE_CATEGORISATION", "GLOBAL_SEARCH"))
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
              .inactive()
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
          .jsonPath("$.content[0].username").isEqualTo("aaabella.moulin")
          .jsonPath("$.content[0].active").isEqualTo(true)

        webTestClient.get().uri {
          it.path("/users/")
            .queryParam("sort", "status,username,desc")
            .build()
        }
          .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content[0].username").isEqualTo("another.user99")
          .jsonPath("$.content[0].active").isEqualTo(false)
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
  @DisplayName("POST /users")
  inner class MaintainUsers {

    @Test
    fun `a database user cannot be created without correct role`() {

      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_DUMMY")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "testuser2",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `a database user can be created`() {

      webTestClient.post().uri("/users/admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateAdminUserRequest(
              username = "testuser2",
              firstName = "Test",
              lastName = "User",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody().json(
          """
          {
          "username": "TESTUSER2",
          "firstName": "TEST",
          "lastName": "USER",
          "activeCaseloadId" : "CADM_I",
          "active": true,
          "accountStatus": "EXPIRED",
          "primaryEmail": "test@test.com",
          "accountType": "ADMIN"
          }
          """
        )

      webTestClient.get().uri("/users/TESTUSER2")
        .headers(setAuthorisation(roles = listOf("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("username").isEqualTo("TESTUSER2")
        .jsonPath("firstName").isEqualTo("TEST")
        .jsonPath("lastName").isEqualTo("USER")
        .jsonPath("staffId").exists()
    }

    @Test
    fun `a user can be link to an existing account`() {

      webTestClient.post().uri("/users/admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateAdminUserRequest(
              username = "testuser4",
              firstName = "Test",
              lastName = "User",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users/link-general-account/TESTUSER4")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedGeneralUserRequest(
              username = "testuser5",
              defaultCaseloadId = "BXI"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated
    }

    @Test
    fun `a user cannot be link to an existing account of same ADMIN type`() {

      webTestClient.post().uri("/users/admin-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateAdminUserRequest(
              username = "testuser6",
              firstName = "Test",
              lastName = "User",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users/link-admin-account/TESTUSER6")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedAdminUserRequest(
              username = "testuser5"
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("User already exists: Admin user already exists for this staff member")
    }

    @Test
    fun `a user cannot be link to an existing account of same GENERAL type`() {

      webTestClient.post().uri("/users/general-account")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateGeneralUserRequest(
              username = "testuser7",
              firstName = "Test",
              lastName = "User",
              defaultCaseloadId = "BXI",
              email = "test@test.com"
            )
          )
        )
        .exchange()
        .expectStatus().isCreated

      webTestClient.post().uri("/users/link-general-account/TESTUSER7")
        .headers(setAuthorisation(roles = listOf("ROLE_CREATE_USER")))
        .body(
          BodyInserters.fromValue(
            CreateLinkedGeneralUserRequest(
              username = "testuser8",
              defaultCaseloadId = "BXI"
            )
          )
        )
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
        .jsonPath("userMessage").isEqualTo("User already exists: General user already exists for this staff member")
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
