package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Information")
data class UserDetail(
  @Schema(description = "Username", example = "testuser1", required = true) val username: String,
  @Schema(description = "Staff ID", example = "324323", required = true) val staffId: Long,
  @Schema(description = "First name of the user", example = "John", required = true) val firstName: String,
  @Schema(description = "Last name of the user", example = "Smith", required = true) val lastName: String,
  @Schema(description = "Active Caseload of the user", example = "BXI", required = false) val activeCaseloadId: String?,
  @Schema(description = "Status of the user", example = "OPEN", required = true) val accountStatus: AccountStatus?,
  @Schema(description = "Type of user account", example = "GENERAL", required = true) val accountType: UsageType = UsageType.GENERAL,
  @Schema(description = "Email addresses of user", example = "test@test.com", required = false) val primaryEmail: String?,
  @Schema(description = "List of associated DPS Role Codes", required = false) val dpsRoleCodes: List<String>,
  @Schema(description = "Account is not locked", required = true) val accountNonLocked: Boolean,
  @Schema(description = "Credentials are not expired flag", required = true) val credentialsNonExpired: Boolean,
  @Schema(description = "User is enabled flag", required = true) val enabled: Boolean,
  @Schema(description = "User is admin flag", required = true) val admin: Boolean,
  @Schema(description = "User is active flag", required = true) val active: Boolean,
) {
  constructor(
    userPersonDetail: UserPersonDetail,
    accountDetail: AccountDetail,
    accountNonLocked: Boolean,
    credentialsNonExpired: Boolean,
    enabled: Boolean,
    admin: Boolean,
    active: Boolean
  ) :
    this(
      username = userPersonDetail.username,
      staffId = userPersonDetail.staff.staffId,
      firstName = userPersonDetail.staff.firstName,
      lastName = userPersonDetail.staff.lastName,
      activeCaseloadId = userPersonDetail.activeCaseLoad?.id,
      accountStatus = accountDetail.status,
      accountType = userPersonDetail.type,
      primaryEmail = userPersonDetail.staff.primaryEmail()?.email,
      dpsRoleCodes = userPersonDetail.dpsRoles.map { it.role.code },
      accountNonLocked = accountNonLocked,
      credentialsNonExpired = credentialsNonExpired,
      enabled = enabled,
      admin = admin,
      active = active,
    )
}
