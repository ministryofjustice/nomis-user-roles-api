@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserBasicDetails
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountProfile
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.AccountStatus
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPassword
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserAndEmailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserBasicDetailsRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserBasicPersonalDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserLastNameRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPasswordRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDateTime
import java.util.Optional

internal class UserServiceTest {
  private val userPersonDetailRepository: UserPersonDetailRepository = mock()
  private val userAndEmailRepository: UserAndEmailRepository = mock()
  private val caseloadRepository: CaseloadRepository = mock()
  private val staffRepository: StaffRepository = mock()
  private val roleRepository: RoleRepository = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder = mock()
  private val passwordEncoder: PasswordEncoder = mock()
  private val userPasswordRepository: UserPasswordRepository = mock()
  private val userBasicDetailsRepository: UserBasicDetailsRepository = mock()
  private val userLastNameRepository: UserLastNameRepository = mock()
  private val userService: UserService = UserService(
    userPersonDetailRepository,
    userAndEmailRepository,
    caseloadRepository,
    staffRepository,
    roleRepository,
    telemetryClient,
    hmppsAuthenticationHolder,
    passwordEncoder,
    userPasswordRepository,
    userBasicDetailsRepository,
    userLastNameRepository,
    recordLogonDate = true,
  )

  @Nested
  internal inner class recordLogonDate {
    @Test
    fun `logon date recorded if user exists`() {
      whenever(userPersonDetailRepository.findById(anyString())).thenReturn(Optional.of(mock()))
      userService.recordLogonDate("user1")
      verify(userPersonDetailRepository).recordLogonDate("user1")
      verify(telemetryClient).trackEvent("NURA-record-logon-date", mapOf("username" to "user1"), null)
    }

    @Test
    fun `logon date not recorded if user does not exist`() {
      whenever(userPersonDetailRepository.findById(anyString())).thenReturn(Optional.empty())
      assertThatThrownBy { userService.recordLogonDate("user1") }.isInstanceOf(UserNotFoundException::class.java)
      verify(userPersonDetailRepository, never()).recordLogonDate(anyString())
      verifyNoInteractions(telemetryClient)
    }
  }

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
        assertThat(AccountDetail(accountStatus = accountStatus.desc).isAccountNonLocked()).isTrue
      }

      val lockedAccountStatuses = filterSetFromAccountStatuses(nonLockedAccountStatuses)
      for (accountStatus in lockedAccountStatuses) {
        assertThat(AccountDetail(accountStatus = accountStatus.desc).isAccountNonLocked()).isFalse
      }
    }

    @Test
    fun `isCredentialsNonExpired validate accountStatuses`() {
      val expiredAccountStatuses =
        setOf(AccountStatus.EXPIRED, AccountStatus.EXPIRED_LOCKED, AccountStatus.EXPIRED_LOCKED_TIMED)
      for (accountStatus in expiredAccountStatuses) {
        assertThat(AccountDetail(accountStatus = accountStatus.desc).isCredentialsNonExpired()).isFalse
      }

      val notExpiredAccountStatuses = filterSetFromAccountStatuses(expiredAccountStatuses)
      for (accountStatus in notExpiredAccountStatuses) {
        assertThat(AccountDetail(accountStatus = accountStatus.desc).isCredentialsNonExpired()).isTrue
      }
    }

    @Test
    fun `isCredentialsNonExpired validate passwordExpiry`() {
      val accountDetailPasswordExpired = AccountDetail(

        accountStatus = AccountStatus.OPEN.desc,
        AccountProfile.TAG_GENERAL.name,
        LocalDateTime.now().minusMinutes(1),
      )
      assertThat(accountDetailPasswordExpired.isCredentialsNonExpired()).isFalse

      val accountDetailPasswordValid = AccountDetail(
        accountStatus = AccountStatus.OPEN.desc,
        AccountProfile.TAG_GENERAL.name,
        LocalDateTime.now().plusMinutes(1),
      )
      assertThat(accountDetailPasswordValid.isCredentialsNonExpired()).isTrue
    }

    @Test
    fun `isEnabled`() {
      // When - enabledAccountStatuses and active user
      val enabledAccountStatuses = setOf(AccountStatus.OPEN, AccountStatus.EXPIRED, AccountStatus.EXPIRED_GRACE)
      for (accountStatus in enabledAccountStatuses) {
        val user = UserPersonDetail(
          username = "raj.maki",
          staff = Staff(
            staffId = 99,
            firstName = "RAJ BOB",
            lastName = "MAKI",
            status = "ACTIVE",
          ),
          type = UsageType.GENERAL,
          activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
          accountDetail = AccountDetail(accountStatus = accountStatus.desc),
        )

        assertThat(user.isEnabled()).isTrue
      }
      // When - disabledAccountStatuses and active user
      val disabledAccountStatuses = filterSetFromAccountStatuses(enabledAccountStatuses)
      for (accountStatus in disabledAccountStatuses) {
        val user = UserPersonDetail(
          username = "raj.maki",
          staff = Staff(
            staffId = 99,
            firstName = "RAJ BOB",
            lastName = "MAKI",
            status = "ACTIVE",
          ),
          type = UsageType.GENERAL,
          activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
          accountDetail = AccountDetail(accountStatus = accountStatus.desc),
        )
        assertThat(user.isEnabled()).isFalse
      }
      // When - disabledAccountStatuses and inactive user
      for (accountStatus in disabledAccountStatuses) {
        val inactiveUser = UserPersonDetail(
          username = "raj.maki",
          staff = Staff(
            staffId = 99,
            firstName = "RAJ BOB",
            lastName = "MAKI",
            status = "INACTIVE",
          ),
          type = UsageType.GENERAL,
          activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
          accountDetail = AccountDetail(accountStatus = accountStatus.desc),
        )

        assertThat(inactiveUser.isEnabled()).isFalse
      }
    }

    @Test
    fun `isAdmin`() {
      val accountDetailIsAdmin = AccountDetail(
        "user",
        profile = AccountProfile.TAG_ADMIN.name,
      )
      assertThat(accountDetailIsAdmin.isAdmin()).isTrue

      val accountDetailIsNotAdmin = AccountDetail(
        "user",
        profile = AccountProfile.TAG_GENERAL.name,
      )
      assertThat(accountDetailIsNotAdmin.isAdmin()).isFalse
    }

    @Test
    fun `isActive`() {
      // Given
      val activeUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.OPEN.desc),
      )
      assertThat(activeUser.isActive()).isTrue

      val inactiveUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "INACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.LOCKED.desc),
      )
      assertThat(inactiveUser.isActive()).isFalse

      // Given
      val expiredUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.EXPIRED.desc),
      )
      assertThat(expiredUser.isActive()).isFalse
    }

    @Test
    fun `isExpired`() {
      // Given
      val activeUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.OPEN.desc),
      )
      assertThat(activeUser.accountDetail?.isExpired()).isFalse

      val graceUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "INACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.EXPIRED_GRACE.desc),
      )
      assertThat(graceUser.accountDetail?.isExpired()).isTrue

      val inactiveUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "INACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.LOCKED.desc),
      )
      assertThat(inactiveUser.accountDetail?.isExpired()).isFalse

      // Given
      val expiredUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.EXPIRED.desc),
      )
      assertThat(expiredUser.accountDetail?.isExpired()).isTrue
    }

    @Test
    fun `isLocked`() {
      // Given
      val activeUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.OPEN.desc),
      )
      assertThat(activeUser.accountDetail?.isAccountNonLocked()).isTrue

      val graceUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.EXPIRED_GRACE.desc),
      )
      assertThat(graceUser.accountDetail?.isAccountNonLocked()).isTrue

      val inactiveUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "INACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.LOCKED.desc),
      )
      assertThat(inactiveUser.accountDetail?.isAccountNonLocked()).isFalse

      // Given
      val expiredUser = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(
          staffId = 99,
          firstName = "RAJ BOB",
          lastName = "MAKI",
          status = "ACTIVE",
        ),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH Hmped (HMP & HMPYOI)"),
        accountDetail = AccountDetail(accountStatus = AccountStatus.EXPIRED.desc),
      )
      assertThat(expiredUser.accountDetail?.isAccountNonLocked()).isTrue
    }

    private fun filterSetFromAccountStatuses(accountStatusesToFilter: Set<AccountStatus>) = AccountStatus.values().filterNot { accountStatusesToFilter.contains(it) }
  }

  @Nested
  internal inner class GetUserBasicDetails {
    @Test
    fun getUserBasicDetails() {
      val userBasicPersonalDetail =
        UserBasicPersonalDetail(
          username = "joe",
          accountStatus = "EXPIRED(GRACE)",
          activeCaseloadId = "MKI",
          firstName = "JOE",
          lastName = "SMITH",
          staffId = 1,

        )

      val expectedUserBasicDetail =
        UserBasicDetails(
          username = "joe",
          accountStatus = "EXPIRED_GRACE",
          activeCaseloadId = "MKI",
          firstName = "Joe",
          lastName = "Smith",
          staffId = 1,
          enabled = true,

        )
      whenever(userBasicDetailsRepository.find(anyString())).thenReturn(Optional.of(userBasicPersonalDetail))
      val userBasicDetails = userService.findUserBasicDetails("joe")
      assertThat(userBasicDetails).isEqualTo(expectedUserBasicDetail)
    }
  }
}
