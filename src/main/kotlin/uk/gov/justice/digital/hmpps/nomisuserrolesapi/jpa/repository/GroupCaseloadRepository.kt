package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.GroupCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.GroupCaseloadPk

@Repository
interface GroupCaseloadRepository : CrudRepository<GroupCaseload, GroupCaseloadPk> {
  fun findAllById_Caseload(caseloadId: String): List<GroupCaseload>
}
