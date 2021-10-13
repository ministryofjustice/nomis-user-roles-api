package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PasswordGeneratorTest {

  @Test
  fun `Test Password`() {

    for (i in 1..20) {
      val password = generatePassword()
      assertThat(password).containsPattern("[A-Za-z0-9]{30}")
    }
  }
}
