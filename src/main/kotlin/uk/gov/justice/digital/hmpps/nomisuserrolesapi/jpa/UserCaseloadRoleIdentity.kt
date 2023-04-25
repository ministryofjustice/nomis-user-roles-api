package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class UserCaseloadRoleIdentity(
  @Column(name = "role_id")
  val roleId: Long,

  @Column
  val username: String,

  @Column(name = "caseload_id")
  val caseload: String,
) : Serializable
