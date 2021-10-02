package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonCaseload(
  @Schema(description = "identify for caseload", example = "WWI")
  val id: String,
  @Schema(description = "description of caseload, typically prison name", example = "WANDSWORTH (HMP)")
  val description: String
)
