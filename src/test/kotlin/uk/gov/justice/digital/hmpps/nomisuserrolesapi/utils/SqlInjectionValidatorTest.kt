package uk.gov.justice.digital.hmpps.nomisuserrolesapi.utils

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SqlInjectionValidatorTest {

  private val validator = SqlInjectionValidator()

  @Test
  fun `should pass validation for safe input`() {
    val safeInputs = listOf(
      "SafeString",
      "ValidUsername123",
      "John Doe",
      "email@example.com",
      "normal_input",
    )

    safeInputs.forEach { input ->
      assertDoesNotThrow { validator.validate(input) }
    }
  }

  @Test
  fun `should throw exception for unsafe input with SQL keywords`() {
    val unsafeInputs = listOf(
      "SELECT * FROM users;",
      "DROP TABLE users;",
      "INSERT INTO users VALUES ('admin', 'password');",
      "exec xp_cmdshell('dir')",
      "UNION SELECT * FROM users",
      "ALTER TABLE users ADD COLUMN password_hash;",
    )

    unsafeInputs.forEach { input ->
      val exception = assertThrows(IllegalArgumentException::class.java) {
        validator.validate(input)
      }
      assert(exception.message!!.contains("Invalid input: \"$input\" contains potentially dangerous SQL characters or keywords."))
    }
  }

  @Test
  fun `should throw exception for unsafe input with special characters`() {
    val unsafeInputs = listOf(
      "John';--",
      "\" OR 1=1 --",
      "(SELECT * FROM users)",
      "password = 'admin' -- DROP TABLE users;",
    )

    unsafeInputs.forEach { input ->
      val exception = assertThrows(IllegalArgumentException::class.java) {
        validator.validate(input)
      }
      assert(exception.message!!.contains("Invalid input: \"$input\" contains potentially dangerous SQL characters or keywords."))
    }
  }

  @Test
  fun `should throw exception for unsafe patterns at any position in the string`() {
    val unsafeInputs = listOf(
      "Normal input followed by SELECT *",
      "DROP something here",
      "Some exec code",
    )

    unsafeInputs.forEach { input ->
      val exception = assertThrows(IllegalArgumentException::class.java) {
        validator.validate(input)
      }
      assert(exception.message!!.contains("Invalid input: \"$input\" contains potentially dangerous SQL characters or keywords."))
    }
  }

  @Test
  fun `should pass validation for safe inputs in a list`() {
    val safeInputs = listOf("SafeString", "ValidUser123", "email@domain.com", "normal_input")

    assertDoesNotThrow { validator.validate(safeInputs) }
  }

  @Test
  fun `should throw exception for unsafe inputs in a list`() {
    val unsafeInputs = listOf("ValidInput", "DROP TABLE users;")

    val exception = assertThrows(IllegalArgumentException::class.java) {
      validator.validate(unsafeInputs)
    }
    assert(exception.message!!.contains("Invalid input: \"DROP TABLE users;\" contains potentially dangerous SQL characters or keywords."))
  }

  @Test
  fun `should handle null and blank inputs gracefully`() {
    val inputs = listOf(null, "", " ")

    assertDoesNotThrow { validator.validate(inputs) }
  }

  @Test
  fun `should validate against all patterns in disallowedPatterns`() {
    val patterns = listOf(
      "--", ";", "'", "\"", "\\*", "\\(", "\\)", "\\=", "\\<", "\\>", "\\|", "\\?",
      "\\[", "\\]", "\\{", "\\}", "exec", "union", "select", "insert", "delete",
      "update", "drop", "alter", "randomblob",
    )

    patterns.forEach { pattern ->
      val input = "Invalid pattern test: $pattern"
      val exception = assertThrows(IllegalArgumentException::class.java) {
        validator.validate(input)
      }
      assert(exception.message!!.contains("Invalid input: \"$input\" contains potentially dangerous SQL characters or keywords."))
    }
  }
}
