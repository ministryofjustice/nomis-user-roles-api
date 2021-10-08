package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer

import org.apache.commons.text.WordUtils
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

fun UserPersonDetail.toUserSummary(): UserSummary = UserSummary(
  username = this.username,
  staffId = this.staff.staffId,
  firstName = this.staff.firstName.capitalizeFully(),
  lastName = this.staff.lastName.capitalizeFully(),
  active = this.staff.isActive,
  activeCaseload = this.activeCaseLoad?.let { caseload ->
    PrisonCaseload(
      id = caseload.id,
      name = caseload.name
    )
  },
)

val userSummaryToEntityPropertyMap = mapOf(
  "firstName" to "staff.firstName",
  "lastName" to "staff.lastName",
  "status" to "staff.status",
  "activeCaseload" to "activeCaseLoad.id",
)

internal fun mapUserSummarySortProperties(sort: String): String =
  userSummaryToEntityPropertyMap[sort] ?: sort

private fun String.capitalizeFully() = WordUtils.capitalizeFully(this)
