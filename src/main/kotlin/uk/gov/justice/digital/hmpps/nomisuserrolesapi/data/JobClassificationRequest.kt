package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class JobClassificationRequest(
  @field:NotBlank("position must not be blank")
  val position: String,
  @field:NotBlank("schedule type must not be blank")
  val scheduleType: String,
  @field:Min(value = 0, message = "hours per week must be a positive integer")
  val hoursPerWeek: Int,
  @field:PastOrPresent("from date must be today or in the past")
  val fromDate: LocalDate,
  @field:PastOrPresent("to date must be null, today or in the past")
  val toDate: LocalDate?,
)
