package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.capitalizeFully
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toUserCaseloadDetail

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Staff Information")
data class StaffDetail(
  @Schema(description = "Staff ID", example = "324323", required = true) val staffId: Long,
  @Schema(description = "First name of the user", example = "John", required = true) val firstName: String,
  @Schema(description = "Last name of the user", example = "Smith", required = true) val lastName: String,
  @Schema(description = "Status of staff account", example = "Smith", required = true) val status: String,
  @Schema(
    description = "Email addresses of staff",
    example = "test@test.com",
    required = false,
  ) val primaryEmail: String?,
  @Schema(
    description = "General user account for this staff member",
    required = false,
  ) val generalAccount: UserCaseloadDetail?,
  @Schema(
    description = "Admin user account for this staff member",
    required = false,
  ) val adminAccount: UserCaseloadDetail?,
) {
  constructor(staff: Staff) :
    this(
      staffId = staff.staffId,
      firstName = staff.firstName.capitalizeFully(),
      lastName = staff.lastName.capitalizeFully(),
      status = staff.status,
      primaryEmail = staff.primaryEmail()?.emailCaseSensitive,
      generalAccount = staff.generalAccount()?.toUserCaseloadDetail(),
      adminAccount = staff.adminAccount()?.toUserCaseloadDetail(),
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User & Caseload Information")
data class UserCaseloadDetail(
  @Schema(description = "Username", example = "TESTUSER1", required = true) val username: String,
  @Schema(description = "Indicates that the user is active", example = "true", required = true) val active: Boolean,
  @Schema(
    description = "Type of user account",
    example = "GENERAL",
    required = true,
  ) val accountType: UsageType = UsageType.GENERAL,
  @Schema(
    description = "Active Caseload of the user",
    example = "BXI",
    required = false,
  ) val activeCaseload: PrisonCaseload?,
  @Schema(
    description = "Caseloads available for this user",
    required = false,
  ) val caseloads: List<PrisonCaseload> = listOf(),
)
