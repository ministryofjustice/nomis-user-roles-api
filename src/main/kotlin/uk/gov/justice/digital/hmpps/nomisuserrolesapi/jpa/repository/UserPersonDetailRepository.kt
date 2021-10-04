package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

@Repository
interface UserPersonDetailRepository :
  JpaRepository<UserPersonDetail, String>,
  JpaSpecificationExecutor<UserPersonDetail>
