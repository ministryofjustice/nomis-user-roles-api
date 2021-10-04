package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import UserFilter
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
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.DataBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupAdministrator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
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
        .transform { user -> user.copy(activeAndInactiveAdministratorOfUserGroups = user.activeAndInactiveAdministratorOfUserGroups.map(makeWWIInactive)) }
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
        .transform { user -> user.copy(activeAndInactiveMemberOfUserGroups = user.activeAndInactiveMemberOfUserGroups.map { makeInactive(it) }) }
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
    @DisplayName("with a filter specification")
    inner class Specification {
      @Test
      internal fun `will return all users for a page when local administrator username not supplied`() {
        val janeBubblesAdministeredUsers = repository.findAll(UserSpecification(UserFilter()), PageRequest.of(0, 10))
        assertThat(janeBubblesAdministeredUsers.content).hasSize(5)
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

        val janeBubblesAdministeredUsers = repository.findAll(UserSpecification(UserFilter("jane.bubbles.wwi")), PageRequest.of(0, 10))
        assertThat(janeBubblesAdministeredUsers.content).hasSize(3)
        val jimHongAdministeredUsers = repository.findAll(UserSpecification(UserFilter("jim.hong.bxi")), PageRequest.of(0, 10))
        assertThat(jimHongAdministeredUsers.content).hasSize(2)
      }
    }
  }
}

fun List<UserGroupAdministrator>.userGroupOf(userGroupCode: String) =
  this.find { it.id.userGroupCode == userGroupCode }!!.userGroup
