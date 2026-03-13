package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

interface UserPersonDetailPageRepository {
  fun findPageOfIds(spec: Specification<UserPersonDetail>?, pageable: Pageable): Page<String>
}
