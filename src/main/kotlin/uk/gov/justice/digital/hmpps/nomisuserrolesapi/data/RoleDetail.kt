package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.RoleType

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role Information")
data class RoleDetail(

  @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true)
  val code: String,

  @Schema(description = "Role Name", example = "Global Search Role", required = true)
  val name: String,

  @Schema(description = "The listing order", example = "1", required = false, defaultValue = "1")
  val sequence: Int = 1,

  @Schema(description = "Role Type ", example = "APP", required = false, defaultValue = "APP")
  val type: RoleType? = RoleType.APP,

  @Schema(description = "If the role is for admin users only", example = "true", required = false, defaultValue = "false")
  val adminRoleOnly: Boolean = false,

  @Schema(description = "Parent Role Code", example = "GLOBAL_SEARCH", required = false)
  val parentRole: RoleDetail? = null,
)
