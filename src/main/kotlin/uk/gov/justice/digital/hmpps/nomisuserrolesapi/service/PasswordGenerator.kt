package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.apache.commons.lang3.RandomStringUtils

/**
 * Generate a password of 30 chars with the following rules
 * <ul>
 *     <li>10 Uppercase chars</li>
 *     <li>10 Lowercase chars</li>
 *     <li>10 digits (0-9)</li>
 * </ul>
 */
fun generatePassword(): String {
  val upperCaseLetters = RandomStringUtils.random(10, 65, 90, true, false)
  val lowerCaseLetters = RandomStringUtils.random(10, 97, 122, true, false)
  val numbers = RandomStringUtils.randomNumeric(10)
  val combinedChars = upperCaseLetters + lowerCaseLetters + numbers

  return combinedChars.chars().mapToObj { c: Int -> c.toChar() }.toList().shuffled().joinToString(separator = "")
}
