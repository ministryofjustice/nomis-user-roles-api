package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter

import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus

data class UserFilter(
  val localAdministratorUsername: String? = null,
  val name: String? = null,
  val status: UserStatus? = null,
  val activeCaseloadId: String? = null,
  val caseloadId: String? = null,
  val roleCodes: List<String> = listOf(),
  val nomisRoleCode: String? = null,
  var inclusiveRoles: String? = null
)
