@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DomainCodeIdentifier
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceCode
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDomain
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.ReferenceCodeRepository

internal class ReferenceDataServiceTest {
  private val caseloadRepository: CaseloadRepository = mock()
  private val referenceCodeRepository: ReferenceCodeRepository = mock()
  private val referenceDataService = ReferenceDataService(caseloadRepository, referenceCodeRepository)

  @Nested
  internal inner class getActiveEmailDomains {
    @Test
    fun getActiveEmailDomains() {
      val domains = listOf(ReferenceCode(DomainCodeIdentifier(ReferenceDomain.EMAIL_DOMAIN, "BOB"), "description"))
      whenever(referenceCodeRepository.findByDomainCodeIdentifierDomainAndActiveIsTrueAndExpiredDateIsNullOrderByDescription(any()))
        .thenReturn(domains)
      assertThat(referenceDataService.getActiveEmailDomains()).isSameAs(domains)
      verify(referenceCodeRepository).findByDomainCodeIdentifierDomainAndActiveIsTrueAndExpiredDateIsNullOrderByDescription(ReferenceDomain.EMAIL_DOMAIN)
    }
  }
}
