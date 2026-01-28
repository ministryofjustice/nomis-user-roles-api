package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

/**
 * Generate a password of 30 chars with the following rules
 * <ul>
 *     <li>10 Uppercase chars</li>
 *     <li>10 Lowercase chars</li>
 *     <li>10 digits (0-9)</li>
 * </ul>
 */
fun generatePassword(): String {
  val upperCaseLetters = (1..10)
    .map { ('A'..'Z').random() }
    .joinToString("")
  val lowerCaseLetters = (1..10)
    .map { ('a'..'z').random() }
    .joinToString("")
  val numbers = (1..10)
    .map { ('0'..'9').random() }
    .joinToString("")
  val combinedChars = upperCaseLetters + lowerCaseLetters + numbers
  return combinedChars.chars().mapToObj { c: Int -> c.toChar() }.toList().shuffled().joinToString(separator = "")
}
