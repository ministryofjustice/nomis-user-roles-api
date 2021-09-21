package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "DBA_USERS")
data class AccountDetail(
  @Id
  @Column(name = "USERNAME", nullable = false)
  val username: String,

  @Column(name = "ACCOUNT_STATUS", nullable = false)
  val accountStatus: String = AccountStatus.EXPIRED_LOCKED.desc,

  @Column(name = "PROFILE")
  val profile: String = AccountProfile.TAG_GENERAL.name,

  @Column(name = "expiry_date")
  val passwordExpiry: LocalDateTime? = null
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as AccountDetail

    return username == other.username
  }

  override fun hashCode(): Int = username.hashCode()

}
