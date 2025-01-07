package uk.gov.justice.digital.hmpps.nomisuserrolesapi.utils

import org.springframework.stereotype.Component

@Component
class SqlInjectionValidator {
  private val disallowedPatterns = listOf(
    // SQL comment
    "--",
    // Statement delimiter
    ";",
    // Single quote
    "'",
    // Double quote
    "\"",
    // Asterisk (wildcard)
    "\\*",
    // Opening parenthesis
    "\\(",
    // Closing parenthesis
    "\\)",
    // Equals sign
    "\\=",
    // Less than
    "\\<",
    // Greater than
    "\\>",
    // Pipe
    "\\|",
    // Question mark
    "\\?",
    // Square bracket (opening)
    "\\[",
    // Square bracket (closing)
    "\\]",
    // Curly brace (opening)
    "\\{",
    // Curly brace (closing)
    "\\}",
    // Exec command
    "exec",
    // SQL UNION
    "union",
    // SQL SELECT
    "select",
    // SQL INSERT
    "insert",
    // SQL DELETE
    "delete",
    // SQL UPDATE
    "update",
    // SQL DROP
    "drop",
    // SQL ALTER
    "alter",
    // SQLite-specific function
    "randomblob",
  )

  fun validate(input: String?) {
    if (input.isNullOrBlank()) return
    disallowedPatterns.forEach { pattern ->
      val regex = Regex(pattern, RegexOption.IGNORE_CASE)
      if (regex.containsMatchIn(input)) {
        throw IllegalArgumentException("Invalid input: \"$input\" contains potentially dangerous SQL characters or keywords.")
      }
    }
  }

  fun validate(inputs: List<String?>) {
    inputs.forEach { validate(it) }
  }
}
