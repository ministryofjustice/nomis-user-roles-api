package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDateTime
import java.util.EnumSet

@Embeddable
data class AccountDetail(

  @Column(name = "ACCOUNT_STATUS", nullable = false, table = "DBA_USERS")
  val accountStatus: String = AccountStatus.EXPIRED.desc,

  @Column(name = "PROFILE", table = "DBA_USERS")
  val profile: String = AccountProfile.TAG_GENERAL.name,

  @Column(name = "expiry_date", table = "DBA_USERS")
  val passwordExpiry: LocalDateTime? = null,
) {

  val status: AccountStatus
    get() = AccountStatus.get(accountStatus)

  private val accountProfile: AccountProfile
    get() = AccountProfile.valueOf(profile.uppercase())

  fun isAccountNonLocked(): Boolean = !isLocked()

  fun isLocked(): Boolean = AccountStatus.values().filter { it.isLocked }.contains(status)

  fun isCredentialsNonExpired(): Boolean {
    val statusNonExpired =
      !EnumSet.of(AccountStatus.EXPIRED, AccountStatus.EXPIRED_LOCKED, AccountStatus.EXPIRED_LOCKED_TIMED)
        .contains(status)
    return statusNonExpired && (passwordExpiry == null || passwordExpiry.isAfter(LocalDateTime.now()))
  }

  fun isEnabled(): Boolean = isAccountNonLocked()

  fun isExpired(): Boolean = AccountStatus.values().filter { it.isExpired }.contains(status)

  fun isActive(): Boolean = AccountStatus.activeStatuses().contains(status)

  fun isAdmin(): Boolean = accountProfile === AccountProfile.TAG_ADMIN
}
