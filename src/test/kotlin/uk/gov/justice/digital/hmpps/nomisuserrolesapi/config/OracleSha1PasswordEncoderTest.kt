package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OracleSha1PasswordEncoderTest {
  private val oracleSha1PasswordEncoder = OracleSha1PasswordEncoder()

  @Test
  fun matches_testMatchesOraclePassword() {
    val encodedPassword = "S:39BA463D55E5C8936A6798CC37B1347BA8BEC37B6407397EB769BC356F0C"
    assertThat(oracleSha1PasswordEncoder.matches("somepass1", encodedPassword)).isTrue
  }

  @Test
  fun matches_invalidPasswordLength() {
    val encodedPassword = "somepasswordthatshouldbeencoded"
    assertThat(oracleSha1PasswordEncoder.matches("somepass1", encodedPassword)).isFalse
  }

  @Test
  fun matches_nullInput() {
    assertThat(oracleSha1PasswordEncoder.matches("somepass1", null)).isFalse
  }
}
