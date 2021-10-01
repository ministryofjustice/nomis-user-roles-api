package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.generalUserEntityCreator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.localAdministratorEntityCreator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAAdminUser
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAGeneralUser
import java.time.LocalDate
import javax.persistence.EntityManager

@DataJpaTest
@ActiveProfiles("test")
class UserPersonDetailRepositoryTest {
  @Autowired
  lateinit var repository: UserPersonDetailRepository

  @Autowired
  lateinit var localAdminAuthorityRepository: LocalAdminAuthorityRepository

  @Autowired
  lateinit var caseloadRepository: CaseloadRepository

  @Autowired
  lateinit var entityManager: EntityManager

  @Test
  internal fun `can read a user general user`() {
    assertThat(repository.findByIdOrNull("jim.bubbles")).isNull()

    generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
      .username("jim.bubbles")
      .atPrison("WWI")
      .buildAndSave()

    entityManager.clear()

    val user = repository.findByIdOrNull("jim.bubbles")!!

    assertThat(user.username).isEqualTo("jim.bubbles")
    assertThat(user.staff.staffId).isNotNull.isGreaterThan(99L)
    assertThat(user.administeredLinks).hasSize(1).allMatch { it.id.localAuthorityCode == "WWI" }
    assertThat(user.administratorLinks).isEmpty()
    assertThat(user.caseloads).hasSize(1).extracting<String> { it.name }.containsExactly("WANDSWORTH (HMP)")
  }

  @Test
  internal fun `can read a local administrator user`() {
    assertThat(repository.findByIdOrNull("diane.bubbles")).isNull()

    localAdministratorEntityCreator(repository, localAdminAuthorityRepository)
      .username("diane.bubbles")
      .atPrison("WWI")
      .buildAndSave()

    entityManager.clear()

    val user = repository.findByIdOrNull("diane.bubbles")!!

    assertThat(user.username).isEqualTo("diane.bubbles")
    assertThat(user.staff.staffId).isNotNull
    assertThat(user.administratorLinks).hasSize(1).allMatch { it.id.localAuthorityCode == "WWI" }
    assertThat(user.administeredLinks).isEmpty()
  }

  @Nested
  inner class LocalAdministrators {
    @BeforeEach
    internal fun setupGeneralUsers() {
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("jane.wwi")
        .atPrison("WWI")
        .buildAndSave()
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("simon.wwi")
        .atPrison("WWI")
        .buildAndSave()
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("steve.wwi.bxi")
        .atPrisons(listOf("WWI", "BXI"))
        .buildAndSave()
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("claire.bxi")
        .atPrison("BXI")
        .buildAndSave()
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("raj.mdi")
        .atPrison("MDI")
        .buildAndSave()
    }

    @Test
    internal fun `can read a local administrator user that administers general users at multiple prisons`() {
      localAdministratorEntityCreator(repository, localAdminAuthorityRepository)
        .username("jane.bubbles.wwi.bxi")
        .atPrisons(listOf("WWI", "BXI"))
        .buildAndSave()

      localAdministratorEntityCreator(repository, localAdminAuthorityRepository)
        .username("hina.bukhari.mdi")
        .atPrison("MDI")
        .buildAndSave()

      entityManager.clear()

      val janeBubbles = repository.findByIdOrNull("jane.bubbles.wwi.bxi")!!

      assertThat(janeBubbles.administratorLinks).hasSize(2)
      assertThat(janeBubbles.administratorLinks.authorityOf("WWI").administeredUsers)
        .extracting<Pair<String, String>> { it.user.username to it.authority.localAuthorityCode }
        .containsExactly("jane.wwi" to "WWI", "simon.wwi" to "WWI", "steve.wwi.bxi" to "WWI")
      assertThat(janeBubbles.administratorLinks.authorityOf("BXI").administeredUsers)
        .extracting<Pair<String, String>> { it.user.username to it.authority.localAuthorityCode }
        .containsExactly("steve.wwi.bxi" to "BXI", "claire.bxi" to "BXI")

      val hinaBukhari = repository.findByIdOrNull("hina.bukhari.mdi")!!

      assertThat(hinaBukhari.administratorLinks).hasSize(1)
      assertThat(hinaBukhari.administratorLinks.authorityOf("MDI").administeredUsers)
        .extracting<String> { it.user.username }
        .containsExactly("raj.mdi")
    }

    @Test
    internal fun `will filter out local administered prisons where the link is no longer active`() {
      val makeInactive: (link: LAAAdminUser) -> LAAAdminUser =
        { it.copy(active = false, expiryDate = LocalDate.now().minusDays(1)) }
      val makeWWIInactive: (link: LAAAdminUser) -> LAAAdminUser = {
        if (it.authority.localAuthorityCode == "WWI") makeInactive(it) else it
      }

      localAdministratorEntityCreator(repository, localAdminAuthorityRepository)
        .username("jane.bubbles.wwi.bxi")
        .atPrisons(listOf("WWI", "BXI"))
        .build()
        .transform { user -> user.copy(administratorLinks = user.administratorLinks.map(makeWWIInactive)) }
        .save()

      entityManager.clear()

      val janeBubbles = repository.findByIdOrNull("jane.bubbles.wwi.bxi")!!

      assertThat(janeBubbles.allAdministratorLinks).hasSize(2)
      assertThat(janeBubbles.administratorLinks).hasSize(1)
      assertThat(janeBubbles.administratorLinks.authorityOf("BXI").administeredUsers)
        .extracting<Pair<String, String>> { it.user.username to it.authority.localAuthorityCode }
        .containsExactly("steve.wwi.bxi" to "BXI", "claire.bxi" to "BXI")
    }

    @Test
    internal fun `will filter out users whose administration link is no longer active`() {
      val makeInactive: (link: LAAGeneralUser) -> LAAGeneralUser =
        { it.copy(active = false, expiryDate = LocalDate.now().minusDays(1)) }

      localAdministratorEntityCreator(repository, localAdminAuthorityRepository)
        .username("jane.bubbles.wli")
        .atPrison("WLI")
        .buildAndSave()

      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("steve.wli")
        .atPrison("WLI")
        .build()
        .transform { user -> user.copy(administeredLinks = user.administeredLinks.map { makeInactive(it) }) }
        .save()
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("claire.wli")
        .atPrison("WLI")
        .buildAndSave()
      generalUserEntityCreator(repository, localAdminAuthorityRepository, caseloadRepository)
        .username("raj.wli")
        .atPrison("WLI")
        .buildAndSave()

      entityManager.clear()

      val janeBubbles = repository.findByIdOrNull("jane.bubbles.wli")!!

      assertThat(janeBubbles.administratorLinks.authorityOf("WLI").allAdministeredUsers).hasSize(3)
      assertThat(janeBubbles.administratorLinks.authorityOf("WLI").administeredUsers)
        .extracting<String> { it.user.username }
        .containsExactly("claire.wli", "raj.wli")
    }
  }
}

fun List<LAAAdminUser>.authorityOf(authorityCode: String) =
  this.find { it.id.localAuthorityCode == authorityCode }!!.authority
