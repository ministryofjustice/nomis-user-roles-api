package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import java.time.LocalDate

data class StaffJobClassification(
  val agencyId: String,
  val staffId: Long,
  val role: String,
  val position: String,
  val scheduleType: String,
  val hoursPerWeek: Int,
  val fromDate: LocalDate,
  val toDate: LocalDate?,
)
