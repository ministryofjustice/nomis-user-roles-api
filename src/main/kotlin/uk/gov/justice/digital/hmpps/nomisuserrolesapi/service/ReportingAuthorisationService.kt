package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserRoleDetail

@Service
class ReportingAuthorisationService(
  private val reportingApiService: ReportingApiService,
  private val userService: UserService
) {

  fun tryGetAuthorisedUrl(username: String): ReportingAuthorisation =
    userService.getUserRoles(username = username, includeNomisRoles = true)
      .takeIf { it.canAccessNomisReports() }
      ?.let { ReportingAuthorisation(reportingApiService.getReportingUrl(username).url) }
      ?: throw AccessDeniedException(
        "User $username does not have access to Nomis reports"
      )
}

data class ReportingAuthorisation(
  val reportingUrl: String
)

enum class ReportingRoleCodes(val roleCode: String) {
  BUSINESS_OBJECTS_POWER_USER("970"),
  BUSINESS_OBJECTS_INTERACTIVE_USER("980")
}

// TODO: probably not correct - may need to check other roles as well like "900" or "900S"
private fun UserRoleDetail.canAccessNomisReports(): Boolean =
  this.takeIf { it.active }?.nomisRoles?.any { it.roles.any { role -> role.allowableReporting() } } ?: false

private fun RoleDetail.allowableReporting(): Boolean = allowableReportingCodes().contains(this.code)

private fun allowableReportingCodes(): List<String> = ReportingRoleCodes.values().map { it.roleCode }
