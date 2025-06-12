package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

enum class UsageType {
  GENERAL,
  ADMIN,
  ;

  companion object {
    fun from(value: String?): UsageType? = value?.let { v -> entries.find { it.name.lowercase() == v.lowercase() } }
  }
}

fun getUsageType(adminOnly: Boolean) = if (adminOnly) {
  UsageType.ADMIN
} else {
  UsageType.GENERAL
}
