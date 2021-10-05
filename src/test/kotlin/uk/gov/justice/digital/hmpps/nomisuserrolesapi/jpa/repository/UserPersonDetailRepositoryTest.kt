package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import UserSpecification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter.UserFilter
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministrator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import java.time.LocalDate
import javax.persistence.EntityManager

@DataJpaTest
@Import(value = [DataBuilder::class])
@ActiveProfiles("test")
class UserPersonDetailRepositoryTest {
  @Autowired
  lateinit var repository: UserPersonDetailRepository

  @Autowired
  private lateinit var dataBuilder: DataBuilder

  @Autowired
  lateinit var entityManager: EntityManager

  @Test
  internal fun `can read a user general user`() {
    assertThat(repository.findByIdOrNull("jim.bubbles")).isNull()

    dataBuilder.generalUser()
      .username("jim.bubbles")
      .atPrison("WWI")
      .buildAndSave()

    entityManager.clear()

    val user = repository.findByIdOrNull("jim.bubbles")!!

    assertThat(user.username).isEqualTo("jim.bubbles")
    assertThat(user.staff.staffId).isNotNull.isGreaterThan(99L)
    assertThat(user.memberOfUserGroups).hasSize(1).allMatch { it.id.userGroupCode == "WWI" }
    assertThat(user.administratorOfUserGroups).isEmpty()
    assertThat(user.caseloads).hasSize(1).extracting<String> { it.caseload.name }.containsExactly("WANDSWORTH (HMP)")
  }

  @Test
  internal fun `can read a local administrator user`() {
    assertThat(repository.findByIdOrNull("diane.bubbles")).isNull()

    dataBuilder.localAdministrator()
      .username("diane.bubbles")
      .atPrison("WWI")
      .buildAndSave()

    entityManager.clear()

    val user = repository.findByIdOrNull("diane.bubbles")!!

    assertThat(user.username).isEqualTo("diane.bubbles")
    assertThat(user.staff.staffId).isNotNull
    assertThat(user.administratorOfUserGroups).hasSize(1).allMatch { it.id.userGroupCode == "WWI" }
    assertThat(user.memberOfUserGroups).isEmpty()
  }

  @Nested
  inner class LocalAdministrators {
    @BeforeEach
    internal fun setupGeneralUsers() {
      dataBuilder.generalUser()
        .username("jane.wwi")
        .atPrison("WWI")
        .buildAndSave()
      dataBuilder.generalUser()
        .username("simon.wwi")
        .atPrison("WWI")
        .buildAndSave()
      dataBuilder.generalUser()
        .username("steve.wwi.bxi")
        .atPrisons(listOf("WWI", "BXI"))
        .buildAndSave()
      dataBuilder.generalUser()
        .username("claire.bxi")
        .atPrison("BXI")
        .buildAndSave()
      dataBuilder.generalUser()
        .username("raj.mdi")
        .atPrison("MDI")
        .buildAndSave()
    }

    @Test
    internal fun `can read a local administrator user that administers general users at multiple prisons`() {
      dataBuilder.localAdministrator()
        .username("jane.bubbles.wwi.bxi")
        .atPrisons(listOf("WWI", "BXI"))
        .buildAndSave()

      dataBuilder.localAdministrator()
        .username("hina.bukhari.mdi")
        .atPrison("MDI")
        .buildAndSave()

      entityManager.clear()

      val janeBubbles = repository.findByIdOrNull("jane.bubbles.wwi.bxi")!!

      assertThat(janeBubbles.administratorOfUserGroups).hasSize(2)
      assertThat(janeBubbles.administratorOfUserGroups.userGroupOf("WWI").members)
        .extracting<Pair<String, String>> { it.user.username to it.userGroup.id }
        .containsExactly("jane.wwi" to "WWI", "simon.wwi" to "WWI", "steve.wwi.bxi" to "WWI")
      assertThat(janeBubbles.administratorOfUserGroups.userGroupOf("BXI").members)
        .extracting<Pair<String, String>> { it.user.username to it.userGroup.id }
        .containsExactly("steve.wwi.bxi" to "BXI", "claire.bxi" to "BXI")

      val hinaBukhari = repository.findByIdOrNull("hina.bukhari.mdi")!!

      assertThat(hinaBukhari.administratorOfUserGroups).hasSize(1)
      assertThat(hinaBukhari.administratorOfUserGroups.userGroupOf("MDI").members)
        .extracting<String> { it.user.username }
        .containsExactly("raj.mdi")
    }

    @Test
    internal fun `will filter out local administered prisons where the link is no longer active`() {
      val makeInactive: (link: UserGroupAdministrator) -> UserGroupAdministrator =
        { it.copy(active = false, expiryDate = LocalDate.now().minusDays(1)) }
      val makeWWIInactive: (link: UserGroupAdministrator) -> UserGroupAdministrator = {
        if (it.userGroup.id == "WWI") makeInactive(it) else it
      }

      dataBuilder.localAdministrator()
        .username("jane.bubbles.wwi.bxi")
        .atPrisons(listOf("WWI", "BXI"))
        .build()
        .transform { user ->
          user.copy(
            activeAndInactiveAdministratorOfUserGroups = user.activeAndInactiveAdministratorOfUserGroups.map(
              makeWWIInactive
            )
          )
        }
        .save()

      entityManager.clear()

      val janeBubbles = repository.findByIdOrNull("jane.bubbles.wwi.bxi")!!

      assertThat(janeBubbles.activeAndInactiveAdministratorOfUserGroups).hasSize(2)
      assertThat(janeBubbles.administratorOfUserGroups).hasSize(1)
      assertThat(janeBubbles.administratorOfUserGroups.userGroupOf("BXI").members)
        .extracting<Pair<String, String>> { it.user.username to it.userGroup.id }
        .containsExactly("steve.wwi.bxi" to "BXI", "claire.bxi" to "BXI")
    }

    @Test
    internal fun `will filter out users whose administration link is no longer active`() {
      val makeInactive: (link: UserGroupMember) -> UserGroupMember =
        { it.copy(active = false, expiryDate = LocalDate.now().minusDays(1)) }

      dataBuilder.localAdministrator()
        .username("jane.bubbles.wli")
        .atPrison("WLI")
        .buildAndSave()

      dataBuilder.generalUser()
        .username("steve.wli")
        .atPrison("WLI")
        .build()
        .transform { user ->
          user.copy(
            activeAndInactiveMemberOfUserGroups = user.activeAndInactiveMemberOfUserGroups.map {
              makeInactive(
                it
              )
            }
          )
        }
        .save()
      dataBuilder.generalUser()
        .username("claire.wli")
        .atPrison("WLI")
        .buildAndSave()
      dataBuilder.generalUser()
        .username("raj.wli")
        .atPrison("WLI")
        .buildAndSave()

      entityManager.clear()

      val janeBubbles = repository.findByIdOrNull("jane.bubbles.wli")!!

      assertThat(janeBubbles.administratorOfUserGroups.userGroupOf("WLI").activeAndInactiveMembers).hasSize(3)
      assertThat(janeBubbles.administratorOfUserGroups.userGroupOf("WLI").members)
        .extracting<String> { it.user.username }
        .containsExactly("claire.wli", "raj.wli")
    }

    @Nested
    @DisplayName("with a filter specification containing lsa")
    inner class LSASpecification {
      @Test
      internal fun `will return all users for a page when local administrator username not supplied`() {
        val users = repository.findAll(UserSpecification(UserFilter()), PageRequest.of(0, 10))
        assertThat(users.content).hasSize(5)
      }

      @Test
      internal fun `will only return user in groups that the user manages`() {
        dataBuilder.localAdministrator()
          .username("jane.bubbles.wwi")
          .atPrison("WWI")
          .buildAndSave()

        dataBuilder.localAdministrator()
          .username("jim.hong.bxi")
          .atPrison("BXI")
          .buildAndSave()

        val janeBubblesAdministeredUsers =
          repository.findAll(UserSpecification(UserFilter("jane.bubbles.wwi")), PageRequest.of(0, 10))
        assertThat(janeBubblesAdministeredUsers.content).hasSize(3)
        val jimHongAdministeredUsers =
          repository.findAll(UserSpecification(UserFilter("jim.hong.bxi")), PageRequest.of(0, 10))
        assertThat(jimHongAdministeredUsers.content).hasSize(2)
      }
    }
  }

  @Nested
  @DisplayName("find all users with a specification")
  inner class FindUserSpecification {

    @Nested
    @DisplayName("with a name filter")
    inner class NameFilter {
      private val lsaAdministratorAtWandsworth = "riz.marshall"
      private val lsaAdministratorAtBrixton = "bobbly.made"
      private val lsaAdministratorAtBrixtonAndWandsworth = "micky.bishop"

      private fun createUppercaseUserOf(firsName: String, lastName: String, prison: String = "WWI") =
        dataBuilder.generalUser()
          .username("$firsName.$lastName".uppercase())
          .firstName(firsName.uppercase())
          .lastName(lastName.uppercase())
          .atPrison(prison)
          .buildAndSave()

      @BeforeEach
      internal fun createUsers() {
        dataBuilder.localAdministrator()
          .username(lsaAdministratorAtWandsworth)
          .atPrison("WWI")
          .buildAndSave()

        dataBuilder.localAdministrator()
          .username(lsaAdministratorAtBrixton)
          .atPrison("BXI")
          .buildAndSave()

        dataBuilder.localAdministrator()
          .username(lsaAdministratorAtBrixtonAndWandsworth)
          .atPrisons(listOf("BXI", "WWI"))
          .buildAndSave()

        listOf(
          "Ibragim" to "Mihail",
          "Marian" to "Chesed",
          "Leopoldo" to "Chesed",
          "Sawyl" to "Alycia",
          "Sawyl" to "Elbert",
          "Alexander" to "Marian",
        ).forEach { createUppercaseUserOf(it.first, it.second) }

        listOf(
          "Saw" to "Micken",
          "Bob" to "Saw",
        ).forEach { createUppercaseUserOf(it.first, it.second, "BXI") }

        dataBuilder.generalUser()
          .username("JOHN.SMITH")
          .firstName("XXXXX")
          .lastName("XXXXX")
          .atPrison("WWI")
          .buildAndSave()
      }

      @Test
      internal fun `will partial match on first name case insensitive`() {
        val usersMatchingIbragi =
          repository.findAll(UserSpecification(UserFilter(name = "IBraGI")), PageRequest.of(0, 10))
        assertThat(usersMatchingIbragi.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "IBRAGIM.MIHAIL"
        )

        val usersMatchingSawyl =
          repository.findAll(UserSpecification(UserFilter(name = "sawyl")), PageRequest.of(0, 10))
        assertThat(usersMatchingSawyl.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "SAWYL.ALYCIA",
          "SAWYL.ELBERT"
        )
      }

      @Test
      internal fun `will partial match on last name case insensitive`() {
        val usersMatchingMiha = repository.findAll(UserSpecification(UserFilter(name = "Miha")), PageRequest.of(0, 10))
        assertThat(usersMatchingMiha.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "IBRAGIM.MIHAIL"
        )

        val usersMatchingChes = repository.findAll(UserSpecification(UserFilter(name = "ches")), PageRequest.of(0, 10))
        assertThat(usersMatchingChes.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "MARIAN.CHESED",
          "LEOPOLDO.CHESED"
        )
      }

      @Test
      internal fun `will match both first and last name`() {
        val users =
          repository.findAll(UserSpecification(UserFilter(name = "Marian")), PageRequest.of(0, 10))
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "MARIAN.CHESED",
          "ALEXANDER.MARIAN"
        )
      }

      @Test
      internal fun `will match partial username`() {
        val users =
          repository.findAll(UserSpecification(UserFilter(name = "john.sm")), PageRequest.of(0, 10))
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "JOHN.SMITH"
        )
      }

      @Test
      internal fun `will match both names both way around when full name detected`() {
        val usersFirstNameFirst =
          repository.findAll(UserSpecification(UserFilter(name = "Saw Alyc")), PageRequest.of(0, 10))
        assertThat(usersFirstNameFirst.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "SAWYL.ALYCIA",
        )

        val usersLastNameFirst =
          repository.findAll(UserSpecification(UserFilter(name = "Alyc Saw")), PageRequest.of(0, 10))
        assertThat(usersLastNameFirst.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "SAWYL.ALYCIA",
        )

        val userCommaSeparatedName =
          repository.findAll(UserSpecification(UserFilter(name = "ALYCIA,sawyl")), PageRequest.of(0, 10))
        assertThat(userCommaSeparatedName.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "SAWYL.ALYCIA",
        )
      }

      @Test
      internal fun `can combine name and LSA filter`() {
        val usersMatchingSaw =
          repository.findAll(UserSpecification(UserFilter(name = "Saw")), PageRequest.of(0, 10))
        assertThat(usersMatchingSaw.content).extracting<String>(UserPersonDetail::username).containsExactly(
          "SAWYL.ALYCIA", "SAWYL.ELBERT", "SAW.MICKEN", "BOB.SAW",
        )

        val usersMatchingSawForLSAAtWandsworth =
          repository.findAll(
            UserSpecification(
              UserFilter(
                localAdministratorUsername = lsaAdministratorAtWandsworth,
                name = "Saw"
              )
            ),
            PageRequest.of(0, 10)
          )
        assertThat(usersMatchingSawForLSAAtWandsworth.content).extracting<String>(UserPersonDetail::username)
          .containsExactly(
            "SAWYL.ALYCIA", "SAWYL.ELBERT",
          )

        val usersMatchingSawForLSAAtBrixton =
          repository.findAll(
            UserSpecification(
              UserFilter(
                localAdministratorUsername = lsaAdministratorAtBrixton,
                name = "Saw"
              )
            ),
            PageRequest.of(0, 10)
          )
        assertThat(usersMatchingSawForLSAAtBrixton.content).extracting<String>(UserPersonDetail::username)
          .containsExactly(
            "SAW.MICKEN", "BOB.SAW",
          )

        val usersMatchingSawForLSAAtBrixtonAndWandsworth =
          repository.findAll(
            UserSpecification(
              UserFilter(
                localAdministratorUsername = lsaAdministratorAtBrixtonAndWandsworth,
                name = "Saw"
              )
            ),
            PageRequest.of(0, 10)
          )
        assertThat(usersMatchingSawForLSAAtBrixtonAndWandsworth.content).extracting<String>(UserPersonDetail::username)
          .containsExactly(
            "SAWYL.ALYCIA", "SAWYL.ELBERT", "SAW.MICKEN", "BOB.SAW",
          )
      }
    }

    @Nested
    @DisplayName("with a status filter")
    inner class StatusFilter {
      private val lsaAdministratorAtWandsworth = "RIZ.MARSHALL"

      private fun createUserOf(username: String, status: String = "ACTIVE", prison: String = "WWI") =
        dataBuilder.generalUser()
          .username(username.uppercase())
          .firstName(username.split(".")[0].uppercase())
          .lastName(username.split(".")[1].uppercase())
          .atPrison(prison)
          .status(status)
          .buildAndSave()

      @BeforeEach
      internal fun createUsers() {
        dataBuilder.localAdministrator()
          .username(lsaAdministratorAtWandsworth)
          .atPrison("WWI")
          .buildAndSave()

        listOf(
          "IBRAGIM.MIHAIL" to "ACTIVE",
          "MARIAN.CHESED" to "ACTIVE",
          "LEOPOLDO.CHESED" to "INACT",
          "SAWYL.ALYCIA" to "INACT",
          "SAWYL.ELBERT" to "SICK",
        ).forEach { createUserOf(username = it.first, status = it.second) }

        listOf(
          "SAW.MICKEN" to "ACTIVE",
          "BOB.SAW" to "INACT",
        ).forEach { createUserOf(username = it.first, status = it.second, "BXI") }
      }

      @Test
      internal fun `will match all users regardless status not supplied`() {
        val users =
          repository.findAll(UserSpecification(UserFilter(status = null)), PageRequest.of(0, 10))
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactlyInAnyOrder(
          "IBRAGIM.MIHAIL",
          "MARIAN.CHESED",
          "LEOPOLDO.CHESED",
          "SAWYL.ALYCIA",
          "SAWYL.ELBERT",
          "SAW.MICKEN",
          "BOB.SAW",
          "RIZ.MARSHALL",
        )
      }

      @Test
      internal fun `will match all users regardless status when filter is ALL`() {
        val users =
          repository.findAll(UserSpecification(UserFilter(status = UserStatus.ALL)), PageRequest.of(0, 10))
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactlyInAnyOrder(
          "IBRAGIM.MIHAIL",
          "MARIAN.CHESED",
          "LEOPOLDO.CHESED",
          "SAWYL.ALYCIA",
          "SAWYL.ELBERT",
          "SAW.MICKEN",
          "BOB.SAW",
          "RIZ.MARSHALL",
        )
      }

      @Test
      internal fun `will match only active when filter is ACTIVE`() {
        val users =
          repository.findAll(UserSpecification(UserFilter(status = UserStatus.ACTIVE)), PageRequest.of(0, 10))
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactlyInAnyOrder(
          "IBRAGIM.MIHAIL",
          "MARIAN.CHESED",
          "SAW.MICKEN",
          "RIZ.MARSHALL",
        )
      }

      @Test
      internal fun `will match only inactive when filter is INACTIVE`() {
        val users =
          repository.findAll(UserSpecification(UserFilter(status = UserStatus.INACTIVE)), PageRequest.of(0, 10))
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactlyInAnyOrder(
          "LEOPOLDO.CHESED",
          "SAWYL.ALYCIA",
          "BOB.SAW",
        )
      }

      @Test
      internal fun `can combine LSA and status filter`() {
        val users =
          repository.findAll(
            UserSpecification(
              UserFilter(
                localAdministratorUsername = lsaAdministratorAtWandsworth,
                status = UserStatus.ACTIVE
              )
            ),
            PageRequest.of(0, 10)
          )
        assertThat(users.content).extracting<String>(UserPersonDetail::username).containsExactlyInAnyOrder(
          "IBRAGIM.MIHAIL",
          "MARIAN.CHESED",
        )
      }

      @Test
      internal fun `there are some statuses eg SICK that can not be filtered`() {
        val countAllUsersThatCanBeFiltered = repository.findAll(
          UserSpecification(UserFilter(status = UserStatus.INACTIVE)),
          PageRequest.of(0, 10)
        ).totalElements +
          repository.findAll(
            UserSpecification(UserFilter(status = UserStatus.ACTIVE)),
            PageRequest.of(0, 10)
          ).totalElements
        val countAllUsersRegardlessOfStatus = repository.findAll(
          UserSpecification(UserFilter(status = UserStatus.ALL)),
          PageRequest.of(0, 10)
        ).totalElements

        assertThat(countAllUsersRegardlessOfStatus).isGreaterThan(countAllUsersThatCanBeFiltered)
      }
    }
  }
}

fun List<UserGroupAdministrator>.userGroupOf(userGroupCode: String) =
  this.find { it.id.userGroupCode == userGroupCode }!!.userGroup
