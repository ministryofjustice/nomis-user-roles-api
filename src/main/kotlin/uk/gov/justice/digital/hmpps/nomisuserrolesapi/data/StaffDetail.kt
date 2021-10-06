package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Staff Information")
data class StaffDetail(
  @Schema(description = "Staff ID", example = "324323", required = true) val staffId: Long,
  @Schema(description = "First name of the user", example = "John", required = true) val firstName: String,
  @Schema(description = "Last name of the user", example = "Smith", required = true) val lastName: String,
  @Schema(description = "Status of staff account", example = "Smith", required = true) val status: String,
  @Schema(description = "Email addresses of staff", example = "test@test.com", required = false) val primaryEmail: String?,
  @Schema(description = "General user account for this staff member",  required = false) val generalAccount: User?,
  @Schema(description = "Admin user account for this staff member",  required = false) val adminAccount: User?
) {
  constructor(staff: Staff) :
    this(
        staffId = staff.staffId,
        firstName = staff.firstName,
        lastName = staff.lastName,
        status = staff.status,
        primaryEmail = staff.primaryEmail()?.email,
        generalAccount = staff.generalAccount()?.let { User(it) },
        adminAccount = staff.adminAccount()?.let { User(it) }
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Basic User Information")
data class User (
    @Schema(description = "Username", example = "testuser1", required = true) val username: String,
    @Schema(description = "Active Caseload of the user", example = "BXI", required = false) val activeCaseloadId: String?,
    @Schema(description = "Indicates that the user is active", example = "true", required = true) val active: Boolean,
    @Schema(description = "Type of user account", example = "GENERAL", required = true, allowableValues = ["GENERAL", "ADMIN"]) val accountType: String = "GENERAL"
) {
    constructor(userPersonDetail: UserPersonDetail) :
            this(
                username = userPersonDetail.username,
                activeCaseloadId = userPersonDetail.activeCaseLoad?.id,
                active = userPersonDetail.staff.isActive,
                accountType = userPersonDetail.type,
            )
}