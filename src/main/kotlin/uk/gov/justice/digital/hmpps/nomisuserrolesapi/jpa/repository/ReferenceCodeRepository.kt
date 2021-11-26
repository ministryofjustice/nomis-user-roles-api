package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.DomainCodeIdentifier
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceCode
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDomain

interface ReferenceCodeRepository : CrudRepository<ReferenceCode, DomainCodeIdentifier> {
  fun findByDomainCodeIdentifierDomainAndActiveIsTrueAndExpiredDateIsNullOrderByDescription(domain: ReferenceDomain?): List<ReferenceCode>
}
