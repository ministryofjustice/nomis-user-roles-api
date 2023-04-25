package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import java.io.Serializable

@Entity
@Table(name = "OMS_ROLES")
data class Role(
  @Id
  @SequenceGenerator(name = "ROLE_ID", sequenceName = "ROLE_ID", allocationSize = 1)
  @GeneratedValue(generator = "ROLE_ID")
  @Column(name = "ROLE_ID", nullable = false)
  val id: Long = 0,

  @Column(name = "ROLE_CODE", nullable = false, unique = true)
  val code: String,

  @Column(name = "ROLE_NAME", nullable = false)
  var name: String,

  @Column(name = "ROLE_SEQ", nullable = false)
  var sequence: Int = 1,

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "PARENT_ROLE_CODE", nullable = true, referencedColumnName = "ROLE_CODE")
  var parent: Role? = null,

  @Column(name = "ROLE_TYPE", nullable = true)
  @Enumerated(EnumType.STRING)
  var type: RoleType? = RoleType.APP,

  @Column(name = "ROLE_FUNCTION", nullable = false)
  @Enumerated(EnumType.STRING)
  var roleFunction: UsageType = UsageType.GENERAL,

  @Column(name = "SYSTEM_DATA_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  val systemData: Boolean = true,

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "ROLE_ID", updatable = false, insertable = false, nullable = false)
  val usersWithRole: List<UserCaseloadRole> = listOf(),

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "parent")
  val childRoles: List<Role> = listOf(),

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @Where(clause = "LOCAL_AUTHORITY_CODE = '$DPS_CASELOAD'")
  val allowedCaseloads: List<RoleCaseload> = listOf(),

) : Serializable {

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

enum class RoleType {
  APP, INST, COMM
}
