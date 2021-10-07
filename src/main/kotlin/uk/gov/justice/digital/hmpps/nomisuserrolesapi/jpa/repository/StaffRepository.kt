package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff

@Repository
interface StaffRepository : CrudRepository<Staff, Long>
