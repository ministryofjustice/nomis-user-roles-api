package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.GeneralUserBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.LocalAdministratorBuilder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.generalUserEntityCreator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper.localAdministratorEntityCreator
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.LAAAdminUser


@DataJpaTest
@ActiveProfiles("test")
class UserPersonDetailRepositoryTest {
  @Autowired
  lateinit var repository: UserPersonDetailRepository

  @Autowired
  lateinit var localAdminAuthorityRepository: LocalAdminAuthorityRepository
  lateinit var generalUserCreator: GeneralUserBuilder
  lateinit var localAdministratorCreator: LocalAdministratorBuilder

  @BeforeEach
  internal fun setUp() {
    generalUserCreator = generalUserEntityCreator(repository, localAdminAuthorityRepository)
    localAdministratorCreator = localAdministratorEntityCreator(repository, localAdminAuthorityRepository)
  }

  @Test
  internal fun `can read a user general user`() {
    assertThat(repository.findByIdOrNull("jim.bubbles")).isNull()

    generalUserCreator
      .username("jim.bubbles")
      .atPrison("WWI")
      .save()

    val user = repository.findByIdOrNull("jim.bubbles")!!

    assertThat(user.username).isEqualTo("jim.bubbles")
    assertThat(user.staff.staffId).isNotNull.isGreaterThan(99L)
    assertThat(user.administeredLinks).hasSize(1).allMatch { it.id.localAuthorityCode == "WWI" }
    assertThat(user.administratorLinks).isEmpty()
  }

  @Test
  internal fun `can read a local administrator user`() {
    assertThat(repository.findByIdOrNull("diane.bubbles")).isNull()

    localAdministratorCreator
      .username("diane.bubbles")
      .atPrison("WWI")
      .save()

    val user = repository.findByIdOrNull("diane.bubbles")!!

    assertThat(user.username).isEqualTo("diane.bubbles")
    assertThat(user.staff.staffId).isNotNull
    assertThat(user.administratorLinks).hasSize(1).allMatch { it.id.localAuthorityCode == "WWI" }
    assertThat(user.administeredLinks).isEmpty()
  }

  @Test
  internal fun `can read a local administrator user that administers general users`() {
    localAdministratorCreator
      .username("jane.bubbles.wwi.bxi")
      .atPrisons(listOf("WWI", "BXI"))
      .save()

    localAdministratorCreator
      .username("hina.bukhari.mdi")
      .atPrison("MDI")
      .save()

    generalUserCreator
      .username("jane.wwi")
      .atPrison("WWI")
      .save()
    generalUserCreator
      .username("simon.wwi")
      .atPrison("WWI")
      .save()
    generalUserCreator
      .username("steve.wwi.bxi")
      .atPrisons(listOf("WWI", "BXI"))
      .save()
    generalUserCreator
      .username("claire.bxi")
      .atPrison("BXI")
      .save()
    generalUserCreator
      .username("raj.mdi")
      .atPrison("MDI")
      .save()

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
}

fun List<LAAAdminUser>.authorityOf(authorityCode: String) =
  this.find { it.id.localAuthorityCode == authorityCode }!!.authority
