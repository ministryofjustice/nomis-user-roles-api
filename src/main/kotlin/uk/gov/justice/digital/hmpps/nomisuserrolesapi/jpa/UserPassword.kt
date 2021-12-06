package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(schema = "SYS", name = "USER$")
data class UserPassword(
  @Id
  @Column(name = "NAME", nullable = false)
  val username: String,

  @Column(name = "SPARE4")
  val password: String?,
)
