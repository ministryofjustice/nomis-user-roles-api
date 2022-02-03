@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPassword
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.AccountDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserAndEmailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPasswordRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.time.LocalDateTime
import java.util.Optional

internal class UserServiceTest {
  private val userPersonDetailRepository: UserPersonDetailRepository = mock()
  private val userAndEmailRepository: UserAndEmailRepository = mock()
  private val caseloadRepository: CaseloadRepository = mock()
  private val accountDetailRepository: AccountDetailRepository = mock()
  private val staffRepository: StaffRepository = mock()
  private val roleRepository: RoleRepository = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val passwordEncoder: PasswordEncoder = mock()
  private val userPasswordRepository: UserPasswordRepository = mock()
  private val userService: UserService = UserService(
    userPersonDetailRepository,
    userAndEmailRepository,
    caseloadRepository,
    accountDetailRepository,
    staffRepository,
    roleRepository,
    telemetryClient,
    authenticationFacade,
    passwordEncoder,
    userPasswordRepository
  )

  @Nested
  internal inner class authenticateUser {
    @Test
    fun success() {
      whenever(userPasswordRepository.findById(anyString())).thenReturn(Optional.of(UserPassword("joe", "pass1234")))
      whenever(passwordEncoder.matches(anyString(), anyString())).thenReturn(true)
      assertThat(userService.authenticateUser("user", "pass")).isTrue
    }

    @Test
    fun failed() {
      whenever(userPasswordRepository.findById(anyString())).thenReturn(Optional.of(UserPassword("joe", "pass1234")))
      whenever(passwordEncoder.matches(anyString(), anyString())).thenReturn(false)
      assertThat(userService.authenticateUser("user", "pass")).isFalse
    }

    @Test
    fun `not found in database`() {
      assertThat(userService.authenticateUser("user", "pass")).isFalse
      verify(passwordEncoder).matches("pass", null)
    }
  }

  @Nested
  internal inner class userFlags {

    @Test
    fun `isAccountNonLocked validate accountStatuses`() {
      val nonLockedAccountStatuses = setOf(AccountStatus.OPEN, AccountStatus.EXPIRED, AccountStatus.EXPIRED_GRACE)
      for (accountStatus in nonLockedAccountStatuses) {
        assertThat(userService.isAccountNonLocked(accountStatus)).isTrue
      }

      val lockedAccountStatuses = filterSetFromAccountStatuses(nonLockedAccountStatuses)
      for (accountStatus in lockedAccountStatuses) {
        assertThat(userService.isAccountNonLocked(accountStatus)).isFalse
      }
    }

    @Test
    fun `isCredentialsNonExpired validate accountStatuses`() {
      val expiredAccountStatuses = setOf(AccountStatus.EXPIRED, AccountStatus.EXPIRED_LOCKED, AccountStatus.EXPIRED_LOCKED_TIMED)
      for (accountStatus in expiredAccountStatuses) {
        assertThat(userService.isCredentialsNonExpired(AccountDetail("user", accountStatus = accountStatus.desc))).isFalse
      }

      val notExpiredAccountStatuses = filterSetFromAccountStatuses(expiredAccountStatuses)
      for (accountStatus in notExpiredAccountStatuses) {
        assertThat(userService.isCredentialsNonExpired(AccountDetail("user", accountStatus = accountStatus.desc))).isTrue
      }
    }

    @Test
    fun `isCredentialsNonExpired validate passwordExpiry`() {
      val accountDetailPasswordExpired = AccountDetail(
        "user",
        accountStatus = AccountStatus.OPEN.desc,
        AccountProfile.TAG_GENERAL.name,
        LocalDateTime.now().minusMinutes(1)
      )
      assertThat(userService.isCredentialsNonExpired(accountDetailPasswordExpired)).isFalse

      val accountDetailPasswordValid = AccountDetail(
        "user",
        accountStatus = AccountStatus.OPEN.desc,
        AccountProfile.TAG_GENERAL.name,
        LocalDateTime.now().plusMinutes(1)
      )
      assertThat(userService.isCredentialsNonExpired(accountDetailPasswordValid)).isTrue
    }

    @Test
    fun `isEnabled`() {
      // Given
      val activeUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99, firstName = "RAJ BOB", lastName = "MAKI",
          status = "ACTIVE"
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)")
      )
      val inactiveUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99, firstName = "RAJ BOB", lastName = "MAKI",
          status = "INACTIVE"
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)")
      )

      // When - enabledAccountStatuses and active user
      val enabledAccountStatuses = setOf(AccountStatus.OPEN, AccountStatus.EXPIRED, AccountStatus.EXPIRED_GRACE)
      for (accountStatus in enabledAccountStatuses) {
        assertThat(userService.isEnabled(activeUser, accountStatus)).isTrue
      }
      // When - enabledAccountStatuses and inactive user
      for (accountStatus in enabledAccountStatuses) {
        assertThat(userService.isEnabled(inactiveUser, accountStatus)).isFalse
      }

      // When - disabledAccountStatuses and active user
      val disabledAccountStatuses = filterSetFromAccountStatuses(enabledAccountStatuses)
      for (accountStatus in disabledAccountStatuses) {
        assertThat(userService.isEnabled(activeUser, accountStatus)).isFalse
      }
      // When - disabledAccountStatuses and inactive user
      for (accountStatus in disabledAccountStatuses) {
        assertThat(userService.isEnabled(inactiveUser, accountStatus)).isFalse
      }
    }

    @Test
    fun `isAdmin`() {
      val accountDetailIsAdmin = AccountDetail(
        "user",
        profile = AccountProfile.TAG_ADMIN.name
      )
      assertThat(userService.isAdmin(accountDetailIsAdmin)).isTrue

      val accountDetailIsNotAdmin = AccountDetail(
        "user",
        profile = AccountProfile.TAG_GENERAL.name
      )
      assertThat(userService.isAdmin(accountDetailIsNotAdmin)).isFalse
    }

    @Test
    fun `isActive`() {
      // Given
      val activeUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99, firstName = "RAJ BOB", lastName = "MAKI",
          status = "ACTIVE"
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)")
      )
      assertThat(userService.isActive(activeUser)).isTrue

      val inactiveUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99, firstName = "RAJ BOB", lastName = "MAKI",
          status = "INACTIVE"
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)")
      )
      assertThat(userService.isActive(inactiveUser)).isFalse
    }

    private fun filterSetFromAccountStatuses(accountStatusesToFilter: Set<AccountStatus>) =
      AccountStatus.values().filterNot { accountStatusesToFilter.contains(it) }
  }
}
