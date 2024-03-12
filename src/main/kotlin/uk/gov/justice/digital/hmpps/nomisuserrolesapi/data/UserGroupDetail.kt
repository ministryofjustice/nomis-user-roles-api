package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Group Information")
data class UserGroupDetail(
  @Schema(description = "Group code", example = "BXI", required = true) val code: String,
  @Schema(description = "Group description", example = "Brixton (HMP)", required = true) val description: String,
)
