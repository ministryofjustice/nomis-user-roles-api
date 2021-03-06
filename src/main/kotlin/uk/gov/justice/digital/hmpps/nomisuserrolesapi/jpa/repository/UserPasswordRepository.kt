package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPassword

@Suppress("SqlResolve")
@Repository
interface UserPasswordRepository : JpaRepository<UserPassword, String>
