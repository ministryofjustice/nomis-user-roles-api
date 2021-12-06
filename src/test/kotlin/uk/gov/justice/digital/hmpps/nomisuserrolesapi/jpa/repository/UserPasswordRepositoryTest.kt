package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPassword

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
class UserPasswordRepositoryTest {
  @Autowired
  private lateinit var repository: UserPasswordRepository

  @Test
  fun givenATransientEntityItCanBePersisted() {
    val transientEntity = transientEntity()
    val entity = transientEntity.copy()
    val persistedEntity = repository.save(entity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    assertThat(persistedEntity.username).isNotNull
    TestTransaction.start()
    val retrievedEntity = repository.findById(entity.username).orElseThrow()

    // equals only compares the business key columns
    assertThat(retrievedEntity).isEqualTo(transientEntity)
    assertThat(retrievedEntity.username).isEqualTo(transientEntity.username)
    assertThat(retrievedEntity.password).isEqualTo(transientEntity.password)
  }

  private fun transientEntity() = UserPassword(username = "user", password = "pass1234")
}
