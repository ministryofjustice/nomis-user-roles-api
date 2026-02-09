package uk.gov.justice.digital.hmpps.nomisuserrolesapi.batchprocessing

data class UserIdCsv(val userId: String)

data class UserRoleAssignment(
  val userId: String,
  val role: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as UserRoleAssignment

    if (userId != other.userId) return false
    if (role != other.role) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userId.hashCode()
    result = 31 * result + role.hashCode()
    return result
  }
}
