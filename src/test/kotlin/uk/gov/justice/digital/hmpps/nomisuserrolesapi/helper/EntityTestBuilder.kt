@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.helper

import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRoleIdentity
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import java.time.LocalDate

fun userPersonalDetails(username: String = "raj.maki", caseLoadId: String = "WWI", roles: List<String> = listOf()) =
  UserPersonDetail(
    username = username,
    staff = Staff(staffId = 99, firstName = "RAJ", lastName = "MAKI", status = "ACTIVE"),
    type = UsageType.GENERAL,
    activeCaseLoad = Caseload(caseLoadId, "Prison for $caseLoadId"),
    dpsRoles = listOf(),
  ).apply {
    val userPersonDetail = this
    // resolve circular immutable reference with reflection cheat (like JPA would do :-) )
    UserPersonDetail::class.java.getDeclaredField("dpsRoles").apply {
      this.isAccessible = true
      this.set(
        userPersonDetail,
        roles.mapIndexed { index, roleCode -> userPersonDetail.userCaseloadRole(roleId = index.toLong(), roleCode) },
      )
    }
  }

private fun UserPersonDetail.userCaseloadRole(
  roleId: Long = 1,
  roleCode: String = "APPROVE_CATEGORISATION",
  caseLoadId: String = "WWI",
) = UserCaseloadRole(
  id = UserCaseloadRoleIdentity(roleId, this.username, caseLoadId),
  role = Role(code = roleCode, name = "description for $roleCode"),
  UserCaseload(
    id = UserCaseloadPk(caseloadId = caseLoadId, username = this.username),
    caseload = Caseload(caseLoadId, "Prison for $caseLoadId"),
    user = this,
    roles = mutableListOf(),
    startDate = LocalDate.now(),
  ),
)
