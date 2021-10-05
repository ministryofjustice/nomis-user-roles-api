package uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.filter

import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserStatus

data class UserFilter(
  val localAdministratorUsername: String? = null,
  val name: String? = null,
  val status: UserStatus? = null
)
