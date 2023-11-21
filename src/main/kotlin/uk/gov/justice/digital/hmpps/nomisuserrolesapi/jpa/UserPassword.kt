package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "SYS", name = "\"USER$\"")
data class UserPassword(
  @Id
  @Column(name = "NAME", nullable = false)
  val username: String,

  @Column(name = "SPARE4")
  val password: String?,
)
