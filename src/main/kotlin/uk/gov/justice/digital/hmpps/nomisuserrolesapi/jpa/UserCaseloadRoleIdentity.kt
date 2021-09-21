package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class UserCaseloadRoleIdentity(
  @Column(name = "role_id")
  val roleId: Long,

  @Column
  val username: String,

  @Column(name = "caseload_id")
  val caseload: String,
) : Serializable
