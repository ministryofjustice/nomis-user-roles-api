package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.standard

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceData
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataKey

interface ReferenceDataRepository : CrudRepository<ReferenceData, ReferenceDataKey> {
  fun findAllByKeyIn(keys: List<ReferenceDataKey>): List<ReferenceData>
}
