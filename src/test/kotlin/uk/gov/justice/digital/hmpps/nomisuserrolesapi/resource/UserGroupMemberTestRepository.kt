package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMember
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserGroupMemberPk

@Repository
interface UserGroupMemberTestRepository : CrudRepository<UserGroupMember, UserGroupMemberPk>
