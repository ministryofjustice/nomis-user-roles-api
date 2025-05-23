package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.repository.CrudRepository

@Entity
@Table(name = "REFERENCE_CODES")
@Immutable
class ReferenceData(
  @EmbeddedId
  val key: ReferenceDataKey,

  @Column(name = "ACTIVE_FLAG")
  @Convert(converter = YesNoConverter::class)
  val active: Boolean,
) : CodedReference by key

enum class ReferenceDataDomain {
  SCHEDULE_TYP,
  STAFF_POS,
  STAFF_ROLE,
}

interface CodedReference {
  val domain: ReferenceDataDomain
  val code: String
}

@Embeddable
data class ReferenceDataKey(
  @Enumerated(EnumType.STRING)
  override val domain: ReferenceDataDomain,
  override val code: String,
) : CodedReference

interface ReferenceDataRepository : CrudRepository<ReferenceData, ReferenceDataKey> {
  fun findAllByKeyIn(keys: List<ReferenceDataKey>): List<ReferenceData>
}

fun ReferenceDataRepository.findByKeyIn(vararg keys: ReferenceDataKey) = findAllByKeyIn(keys.asList())
infix fun ReferenceDataDomain.of(code: String) = ReferenceDataKey(this, code)
