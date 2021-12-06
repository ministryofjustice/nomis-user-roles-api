@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.security.crypto.password.PasswordEncoder
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.AuthenticationFacade
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPassword
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.AccountDetailRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.RoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPasswordRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.UserPersonDetailRepository
import java.util.Optional

internal class UserServiceTest {
  private val userPersonDetailRepository: UserPersonDetailRepository = mock()
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
}
