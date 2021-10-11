package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User Information")
data class UserDetail(
    @Schema(description = "Username", example = "testuser1", required = true) val username: String,
    @Schema(description = "Staff ID", example = "324323", required = true) val staffId: Long,
    @Schema(description = "First name of the user", example = "John", required = true) val firstName: String,
    @Schema(description = "Last name of the user", example = "Smith", required = true) val lastName: String,
    @Schema(description = "Active Caseload of the user", example = "BXI", required = false) val activeCaseloadId: String?,
    @Schema(description = "Indicates that the user is active", example = "true", required = true) val active: Boolean,
    @Schema(description = "Status of the user", example = "OPEN", required = true) val accountStatus: AccountStatus?,
    @Schema(description = "Type of user account", example = "GENERAL", required = true) val accountType: AccountType = AccountType.GENERAL,
    @Schema(description = "Email addresses of user", example = "test@test.com", required = false) val primaryEmail: String?
) {
  constructor(userPersonDetail: UserPersonDetail, accountDetail: AccountDetail) :
    this(
      username = userPersonDetail.username,
      staffId = userPersonDetail.staff.staffId,
      firstName = userPersonDetail.staff.firstName,
      lastName = userPersonDetail.staff.lastName,
      activeCaseloadId = userPersonDetail.activeCaseLoad?.id,
      active = userPersonDetail.staff.isActive,
      accountStatus = accountDetail.status,
      accountType = userPersonDetail.type,
      primaryEmail = userPersonDetail.staff.primaryEmail()?.email
    )
}
