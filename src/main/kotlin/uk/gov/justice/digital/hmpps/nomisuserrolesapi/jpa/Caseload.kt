package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "CASELOADS")
data class Caseload(
  @Id
  @Column(name = "CASELOAD_ID", nullable = false)
  val id: String,
  @Column(name = "DESCRIPTION", nullable = false)
  val name: String,

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

  override fun hashCode(): Int = id.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(id = $id )"
  }
}
