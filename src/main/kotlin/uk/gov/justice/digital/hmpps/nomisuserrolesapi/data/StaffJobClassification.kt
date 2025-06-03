package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import java.math.BigDecimal
import java.time.LocalDate

data class StaffJobClassification(
  val agencyId: String,
  val staffId: Long,
  val role: String,
  val position: String,
  val scheduleType: String,
  val hoursPerWeek: BigDecimal,
  val fromDate: LocalDate,
  val toDate: LocalDate?,
)
