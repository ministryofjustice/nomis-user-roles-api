package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DomainCodeIdentifier
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceCode
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDomain.EMAIL_DOMAIN

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
class ReferenceCodeRepositoryTest {
  @Autowired
  private lateinit var repository: ReferenceCodeRepository

  @Test
  fun givenATransientEntityItCanBePersisted() {
    val transientEntity = transientEntity()
    val entity = transientEntity.copy()
    val persistedEntity = repository.save(entity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    assertThat(persistedEntity.domainCodeIdentifier).isNotNull
    TestTransaction.start()
    val retrievedEntity = repository.findById(entity.domainCodeIdentifier).orElseThrow()

    // equals only compares the business key columns
    assertThat(retrievedEntity).isEqualTo(transientEntity)
    assertThat(retrievedEntity.description).isEqualTo(transientEntity.description)
    assertThat(retrievedEntity.active).isEqualTo(transientEntity.active)
  }

  @Test
  fun givenAnExistingUserTheyCanBeRetrieved() {
    val retrievedEntity = repository.findById(DomainCodeIdentifier(EMAIL_DOMAIN, "PROBATION")).orElseThrow()
    assertThat(retrievedEntity.description).isEqualTo("HMIProbation.gov.uk")
    assertThat(retrievedEntity.active).isTrue()
  }

  @Test
  fun testFind() {
    val codes = repository.findByDomainCodeIdentifierDomainAndActiveIsTrueAndExpiredDateIsNullOrderByDescription(EMAIL_DOMAIN)
    assertThat(codes).extracting("description").contains("%justice.gov.uk", "HMIProbation.gov.uk").hasSize(16)
  }

  private fun transientEntity() = ReferenceCode(
    domainCodeIdentifier = DomainCodeIdentifier(EMAIL_DOMAIN, "JOE"),
    active = true,
    description = "some description"
  )
}
