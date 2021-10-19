package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload.Companion.GENERAL_CASELOAD
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.CaseloadRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer.toPrisonCaseload

@Service
class ReferenceDataService(private val caseloadRepository: CaseloadRepository) {
  fun getActiveGeneralCaseloads(): List<PrisonCaseload> =
    caseloadRepository.findByActiveAndFunctionOrderByNameAsc(true, GENERAL_CASELOAD).map { it.toPrisonCaseload() }
}
