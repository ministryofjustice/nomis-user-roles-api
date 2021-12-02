package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload.Companion.GENERAL_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload.Companion.INSTITUTION_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toPrisonCaseload

@Service
class ReferenceDataService(private val caseloadRepository: CaseloadRepository) {
  fun getActiveGeneralCaseloads(): List<PrisonCaseload> =
    caseloadRepository.findByActiveAndFunctionAndTypeOrderByNameAsc(true, GENERAL_CASELOAD, INSTITUTION_CASELOAD)
      .map { it.toPrisonCaseload() }
}
