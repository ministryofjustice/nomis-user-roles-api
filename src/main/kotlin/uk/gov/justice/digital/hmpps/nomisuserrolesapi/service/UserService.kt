package uk.gov.justice.digital.hmpps.nomisuserrolesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource.UserDetail
import javax.transaction.Transactional

@Service
@Transactional
class UserService {
  fun findByUsername(username: String) = UserDetail(username, 1, "John", "Smith")
}
