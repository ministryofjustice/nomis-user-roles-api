package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role

@Repository
interface RoleRepository : CrudRepository<Role, Long> {
  fun findByCode(code: String): Role?
}
