package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.RoleType
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Role update")
data class UpdateRoleRequest(

  @Schema(description = "Role Name", example = "Global Search Role", required = false)
  @field:Size(
    max = 30,
    min = 1,
    message = "Name must be between 1 and 30",
  )
  val name: String? = null,

  @Schema(description = "Display Sequence", example = "99", required = false)
  val sequence: Int? = null,

  @Schema(description = "Parent Role Code", example = "LICENCE_ADMIN", required = false)
  @field:Size(
    max = 30,
    min = 1,
    message = "Parent Code must be between 1 and 30",
  )
  val parentRoleCode: String? = null,

  @Schema(description = "Role Type ", example = "APP", required = false)
  val type: RoleType? = null,

  @Schema(description = "adminRoleOnly", example = "true", required = false)
  val adminRoleOnly: Boolean? = null,

)
