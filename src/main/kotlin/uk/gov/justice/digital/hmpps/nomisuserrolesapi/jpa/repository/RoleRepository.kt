package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.RoleType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import java.util.Optional

@Repository
interface RoleRepository : CrudRepository<Role, Long> {
  fun findByCode(code: String): Optional<Role>

  fun findAllByTypeAndRoleFunctionIn(type: RoleType, roleFunctions: List<UsageType>): List<Role>
}
