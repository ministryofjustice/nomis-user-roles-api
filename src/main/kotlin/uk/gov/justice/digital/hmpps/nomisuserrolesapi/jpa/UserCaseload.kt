@file:Suppress("DataClassEqualsAndHashCodeInspection")

package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserRoleAlreadyExistsException
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserRoleNotFoundException
import java.io.Serializable
import java.time.LocalDate
import java.util.Objects
import java.util.function.Supplier

@Embeddable
data class UserCaseloadPk(
  @Column(name = "CASELOAD_ID", nullable = false)
  val caseloadId: String,

  @Column(name = "USERNAME", nullable = false)
  val username: String,
) : Serializable

@Entity
@Table(name = "USER_ACCESSIBLE_CASELOADS")
data class UserCaseload(
  @EmbeddedId
  val id: UserCaseloadPk,

  @ManyToOne
  @JoinColumn(name = "CASELOAD_ID", updatable = false, insertable = false)
  val caseload: Caseload,

  @ManyToOne
  @JoinColumn(name = "USERNAME", updatable = false, insertable = false)
  val user: UserPersonDetail,

  @Column(name = "START_DATE")
  val startDate: LocalDate,

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "userCaseload", orphanRemoval = true)
  val roles: MutableList<UserCaseloadRole> = mutableListOf(),

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as UserCaseload

    return id == other.id
  }

  override fun hashCode(): Int = Objects.hash(id)

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(EmbeddedId = $id )"
  }

  fun addRole(role: Role): UserCaseloadRole {
    findRoleByCode(role.code)?.run {
      throw UserRoleAlreadyExistsException("Role ${role.code} is already assigned to this user")
    }

    if (role.type?.name != caseload.type) {
      throw InvalidRoleAssignmentException("Roles of type ${role.type} cannot be assigned to caseloads of type ${caseload.type}")
    }

    val userCaseloadRole = UserCaseloadRole(
      id = UserCaseloadRoleIdentity(roleId = role.id, username = id.username, caseload = id.caseloadId),
      role = role,
      userCaseload = this,
    )
    roles.add(userCaseloadRole)
    return userCaseloadRole
  }

  fun removeRole(roleCode: String) {
    findRoleByCode(roleCode)?.run {
      roles.remove(this)
    } ?: throw UserRoleNotFoundException("Role $roleCode is not assigned to this user")
  }

  private fun findRoleByCode(roleCode: String) = roles.firstOrNull { r -> r.role.code == roleCode }
}

class InvalidRoleAssignmentException(message: String?) :
  RuntimeException(message),
  Supplier<InvalidRoleAssignmentException> {
  override fun get(): InvalidRoleAssignmentException {
    return InvalidRoleAssignmentException(message)
  }
}
