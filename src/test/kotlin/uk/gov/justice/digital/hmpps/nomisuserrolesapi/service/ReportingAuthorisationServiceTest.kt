package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CaseloadRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType

internal class ReportingAuthorisationServiceTest {
  private val reportingApiService: ReportingApiService = mock()
  private val userService: UserService = mock()

  private val reportingAuthorisationService = ReportingAuthorisationService(reportingApiService, userService)

  @Test
  internal fun `will throw Access Denied error when user not active`() {
    whenever(userService.getUserRoles("bobby.beans", true)).thenReturn(
      userWithRole("980").copy(active = false)
    )

    assertThrows<AccessDeniedException> {
      reportingAuthorisationService.tryGetAuthorisedUrl("bobby.beans")
    }
  }

  @Test
  internal fun `will throw Access Denied error when user does not have either 980 or 970 role`() {
    whenever(userService.getUserRoles("bobby.beans", true)).thenReturn(
      userWithRole("123")
    )

    assertThrows<AccessDeniedException> {
      reportingAuthorisationService.tryGetAuthorisedUrl("bobby.beans")
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["980", "970"])
  internal fun `will return reporting url from reporting API`(roleCode: String) {
    whenever(userService.getUserRoles("bobby.beans", true)).thenReturn(
      userWithRole(roleCode)
    )
    whenever(reportingApiService.getReportingUrl("bobby.beans")).thenReturn(ReportingUrlResponse("https://reporting.com"))

    val urlResponse = reportingAuthorisationService.tryGetAuthorisedUrl("bobby.beans")

    assertThat(urlResponse.reportingUrl).isEqualTo("https://reporting.com")
  }
}

fun userWithRole(role: String): UserRoleDetail {
  return UserRoleDetail(
    active = true,
    username = "bobby.beans",
    accountType = UsageType.GENERAL,
    nomisRoles = listOf(
      CaseloadRoleDetail(
        caseload = PrisonCaseload(id = "MDI", name = "HMP Moorland"),
        roles = listOf(
          RoleDetail(
            code = role,
            name = "Business Objs Interactive User",
            sequence = 1
          )
        )
      ),
      CaseloadRoleDetail(
        caseload = PrisonCaseload(id = "BXI", name = "HMP Brixton"),
        roles = listOf(
          RoleDetail(
            code = "835",
            name = "Works Staff",
            sequence = 1
          )
        )
      )
    )
  )
}
