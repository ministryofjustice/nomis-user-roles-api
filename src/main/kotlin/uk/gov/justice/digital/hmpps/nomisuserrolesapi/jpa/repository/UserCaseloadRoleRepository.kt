package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadRoleIdentity

@Repository
interface UserCaseloadRoleRepository : CrudRepository<UserCaseloadRoleIdentity, UserCaseloadRole> {
  fun findAllById_caseloadAndRole_code(caseload: String, roleCode: String): List<UserCaseloadRole>
}
