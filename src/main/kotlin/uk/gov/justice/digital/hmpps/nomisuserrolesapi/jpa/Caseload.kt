package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter

const val DPS_CASELOAD = "NWEB"
const val CENTRAL_ADMIN_CASELOAD = "CADM_I"

@Entity
@Table(name = "CASELOADS")
data class Caseload(
  @Id
  @Column(name = "CASELOAD_ID", nullable = false)
  val id: String,
  @Column(name = "DESCRIPTION", nullable = false)
  val name: String,
  @Convert(converter = YesNoConverter::class)
  @Column(name = "ACTIVE_FLAG", nullable = false)
  val active: Boolean = true,
  @Column(name = "CASELOAD_FUNCTION", nullable = false)
  val function: String = GENERAL_CASELOAD,
  @Column(name = "CASELOAD_TYPE", nullable = false)
  val type: String = INSTITUTION_CASELOAD,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "CASELOAD_ID")
  val userGroups: List<GroupCaseload> = listOf(),

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Caseload

    return id == other.id
  }

  fun isDpsCaseload(): Boolean = id == DPS_CASELOAD

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = $id )"
  }

  companion object {
    const val GENERAL_CASELOAD = "GENERAL"

    @Suppress("unused")
    const val ADMIN_CASELOAD = "ADMIN"

    const val INSTITUTION_CASELOAD = "INST"

    @Suppress("unused")
    const val APPLICATION_CASELOAD = "APP"
  }
}
