package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.math.BigDecimal
import java.time.LocalDate

data class JobClassificationRequest(
  @field:Schema(description = "The code of the position (based on nomis reference data)", example = "PO", required = true)
  @field:NotBlank("position must not be blank")
  val position: String,
  @field:Schema(description = "The code of the schedule type (based on nomis reference data)", example = "FT", required = true)
  @field:NotBlank("schedule type must not be blank")
  val scheduleType: String,
  @field:Schema(description = "The number of hours per week the staff member works", example = "37.5", required = true)
  @field:Min(value = 0, message = "hours per week must be greater than zero")
  @field:Digits(integer = 4, fraction = 2, message = "hours per week must match nnnn.nn")
  val hoursPerWeek: BigDecimal,
  @field:Schema(description = "The date the classification started (must be today or in the past)", required = true)
  @field:PastOrPresent("from date must be today or in the past")
  val fromDate: LocalDate,
  @field:Schema(description = "The date the classification ended", required = false)
  @field:PastOrPresent("to date must be null, today or in the past")
  val toDate: LocalDate?,
)
