package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer

import org.apache.commons.text.WordUtils
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserSummary
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import java.util.regex.Pattern

fun UserPersonDetail.toUserSummary(): UserSummary = UserSummary(
  username = this.username,
  staffId = this.staff.staffId,
  firstName = this.staff.firstName.capitalizeFully(),
  lastName = this.staff.lastName.capitalizeFully(),
  active = this.staff.isActive,
  activeCaseload = this.activeCaseLoad?.let { caseload ->
    PrisonCaseload(
      id = caseload.id,
      name = caseload.name.capitalizeLeavingAbbreviations()
    )
  },
)

val userSummaryToEntityPropertyMap = mapOf(
  "firstName" to "staff.firstName",
  "lastName" to "staff.lastName",
  "status" to "staff.status",
  "activeCaseload" to "activeCaseLoad.id",
)

fun Role.toRoleDetail(): RoleDetail = RoleDetail(
  code = this.code,
  name = this.name,
  sequence = this.sequence,
  adminRoleOnly = this.roleFunction == UsageType.ADMIN,
  type = this.type,
  parentRole = this.parent?.toRoleDetail()
)

internal fun mapUserSummarySortProperties(sort: String): String =
  userSummaryToEntityPropertyMap[sort] ?: sort

private fun String.capitalizeFully() = WordUtils.capitalizeFully(this)
private fun String.capitalizeLeavingAbbreviations() = AbbreviationsProcessor.capitalizeLeavingAbbreviations(this)

object AbbreviationsProcessor {
  internal fun capitalizeLeavingAbbreviations(value: String): String {
    val description = value.capitalizeFully()
    // Using word boundaries to find the right string ensures we catch the strings
    // wherever they appear in the description, while also avoiding replacing
    // the letter sequence should it appear in the middle of a word
    // e.g. this will not match 'mosaic' even though AIC is one of the abbreviations
    val matcher = pattern.matcher(description)

    // There could be more than one abbreviation in a string,
    // e.g. HMP Moorland VCC Room 1
    // By using the string buffer and the appendReplacement method
    // we ensure that all the matching groups are replaced accordingly
    val stringBuilder = StringBuilder()
    while (matcher.find()) {
      val matched = matcher.group(1)
      matcher.appendReplacement(stringBuilder, matched.uppercase())
    }
    matcher.appendTail(stringBuilder)
    return stringBuilder.toString()
  }

  private val ABBREVIATIONS = listOf(
    "AAA",
    "ADTP",
    "AIC",
    "AM",
    "ATB",
    "BBV",
    "BHU",
    "BICS",
    "CAD",
    "CASU",
    "CES",
    "CGL",
    "CIT",
    "CSC",
    "CSU",
    "CTTLS",
    "CV",
    "DART",
    "DDU",
    "DHL",
    "DRU",
    "ETS",
    "ESOL",
    "FT",
    "GP",
    "GFSL",
    "HCC",
    "HDC",
    "HMP",
    "HMPYOI",
    "HR",
    "IAG",
    "ICT",
    "IDTS",
    "IMB",
    "IPD",
    "IPSO",
    "ISMS",
    "IT",
    "ITQ",
    "JAC",
    "LRC",
    "L&S",
    "MBU",
    "MCASU",
    "MDT",
    "MOD",
    "MPU",
    "NVQ",
    "OBP",
    "OMU",
    "OU",
    "PACT",
    "PASRO",
    "PCVL",
    "PE",
    "PICTA",
    "PIPE",
    "PM",
    "PT",
    "PTTLS",
    "RAM",
    "RAPT",
    "ROTL",
    "RSU",
    "SDP",
    "SIU",
    "SMS",
    "SOTP",
    "SPU",
    "STC",
    "TLC",
    "TSP",
    "UK",
    "VCC",
    "VDT",
    "VP",
    "VTC",
    "WFC",
    "YOI"
  ).joinToString("|")

  private val pattern =
    Pattern.compile("\\b($ABBREVIATIONS)\\b", Pattern.CASE_INSENSITIVE)
}
