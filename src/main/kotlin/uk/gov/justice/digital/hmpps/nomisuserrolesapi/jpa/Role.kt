package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "OMS_ROLES")
data class Role(
  @Id
  @SequenceGenerator(name = "ROLE_ID", sequenceName = "ROLE_ID", allocationSize = 1)
  @GeneratedValue(generator = "ROLE_ID")
  @Column(name = "ROLE_ID", nullable = false)
  val id: Long,

  @Column(name = "ROLE_CODE", nullable = false, unique = true)
  val code: String,

  @Column(name = "ROLE_NAME", nullable = false)
  val name: String,

  @Column(name = "ROLE_SEQ", nullable = false)
  val sequence: Int = 1,

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "PARENT_ROLE_CODE", referencedColumnName = "ROLE_CODE", nullable = true)
  val parent: Role? = null,

  @Column(name = "ROLE_TYPE", nullable = true)
  @Enumerated(EnumType.STRING)
  val type: RoleType = RoleType.APP,

  @Column(name = "ROLE_FUNCTION", nullable = false)
  val roleFunction: String = "GENERAL",

  @Column(name = "SYSTEM_DATA_FLAG", nullable = false)
  @Type(type = "yes_no")
  val systemData: Boolean = false,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Role

    return code == other.code
  }

  override fun hashCode(): Int = code.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(code = $code )"
  }
}

enum class RoleType{
  APP, INST, COMM
}
