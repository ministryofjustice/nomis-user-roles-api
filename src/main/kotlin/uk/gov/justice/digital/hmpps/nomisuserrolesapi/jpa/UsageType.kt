package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

enum class UsageType {
  GENERAL,
  ADMIN,
}

fun getUsageType(adminOnly: Boolean) = if (adminOnly) {
  UsageType.ADMIN
} else {
  UsageType.GENERAL
}
