package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.RoleType
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role Information creation")
data class CreateRoleRequest(

  @Schema(description = "Role Code", example = "GLOBAL_SEARCH", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "Code must be between 1 and 30"
  ) @NotBlank val code: String,

  @Schema(description = "Role Name", example = "Global Search Role", required = true) @field:Size(
    max = 30,
    min = 1,
    message = "Name must be between 1 and 30"
  ) @NotBlank val name: String,

  @Schema(description = "Display Sequence", example = "99", required = false, defaultValue = "1")
  val sequence: Int = 1,

  @Schema(description = "Parent Role Code", example = "LICENCE_ADMIN", required = false) @field:Size(
    max = 30,
    min = 1,
    message = "Parent Code must be between 1 and 30"
  ) val parentRoleCode: String? = null,

  @Schema(description = "Role Type ", example = "APP", required = false, defaultValue = "APP")
  val type: RoleType = RoleType.APP,

  @Schema(description = "Role Code", example = "true", required = false, defaultValue = "false")
  @NotBlank val adminRoleOnly: Boolean = false,

)
