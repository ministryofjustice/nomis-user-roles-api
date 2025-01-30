package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import com.google.common.primitives.Bytes
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.security.crypto.codec.Hex
import org.springframework.security.crypto.keygen.KeyGenerators
import org.springframework.security.crypto.keygen.StringKeyGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Format of oracle sha1 password is:
 * S:[password 40 characters][salt 20 characters]
 * all in uppercase hex - including salt
 */
@Component
class OracleSha1PasswordEncoder : PasswordEncoder {
  private val keyGenerator = KeyGenerators.secureRandom(10) // this generates a salt of 20 characters
  private val saltGenerator: StringKeyGenerator = StringKeyGenerator {
    String(Hex.encode(keyGenerator.generateKey())).uppercase()
  }

  /**
   * Encodes the rawPass. If a salt is specified it will be merged with the password before encoding.
   *
   * @param rawPassword The plain text password
   * @return Hex string of password digest
   */
  override fun encode(rawPassword: CharSequence): String {
    val salt = saltGenerator.generateKey()
    return digest(salt, rawPassword)
  }

  private fun digest(salt: String, rawPassword: CharSequence): String {
    val saltedPassword = Bytes.concat(rawPassword.toString().toByteArray(), Hex.decode(salt))
    return "S:" + DigestUtils.sha1Hex(saltedPassword).uppercase() + salt
  }

  /**
   * Takes a previously encoded password and compares it with the rawPassword after mixing
   * in the salt and encoding that value
   *
   * @param rawPassword     plain text password
   * @param encodedPassword previously encoded password
   * @return true or false
   */
  override fun matches(rawPassword: CharSequence, encodedPassword: String?): Boolean {
    val salt = extractSalt(encodedPassword)
    val rawPasswordEncoded = digest(salt, rawPassword)
    return PasswordEncoderUtils.equals(encodedPassword, rawPasswordEncoded)
  }

  private fun extractSalt(prefixEncodedPassword: String?): String = if (prefixEncodedPassword != null && prefixEncodedPassword.length >= 62) {
    prefixEncodedPassword.substring(
      42,
      62,
    )
  } else {
    ""
  }
}
