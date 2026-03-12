package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.standard

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRoleId

interface StaffLocationRoleRepository : CrudRepository<StaffLocationRole, StaffLocationRoleId> {
  fun findAllByAgencyIdAndStaffIdAndRole(agencyId: String, staffId: Long, role: String): List<StaffLocationRole>
}
