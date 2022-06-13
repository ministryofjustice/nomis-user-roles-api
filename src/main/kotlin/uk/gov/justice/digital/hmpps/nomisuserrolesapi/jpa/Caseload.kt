package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

const val DPS_CASELOAD = "NWEB"
const val CENTRAL_ADMIN_CASELOAD = "CADM_I"

@Entity
@Table(name = "CASELOADS")
@BatchSize(size = 100)
data class Caseload(
  @Id
  @Column(name = "CASELOAD_ID", nullable = false)
  val id: String,
  @Column(name = "DESCRIPTION", nullable = false)
  val name: String,
  @Type(type = "yes_no")
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
