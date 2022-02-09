package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer

import org.apache.commons.text.WordUtils
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.CaseloadRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.RoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserCaseloadDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.UserRoleDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Role
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import java.util.regex.Pattern

fun UserPersonDetail.toUserCaseloadDetail(removeDpsCaseload: Boolean = false): UserCaseloadDetail = UserCaseloadDetail(
  username = this.username,
  activeCaseload = this.activeCaseLoad?.toPrisonCaseload(),
  active = this.staff.isActive,
  accountType = this.type,
  caseloads = this.caseloads
    .filter { !(removeDpsCaseload && it.caseload.isDpsCaseload()) }
    .map { uc ->
      PrisonCaseload(
        id = uc.id.caseloadId,
        name = uc.caseload.name.capitalizeLeavingAbbreviations()
      )
    }
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

fun Staff.toStaffDetail(): StaffDetail = StaffDetail(this)

fun UserPersonDetail.toUserRoleDetail(includeNomisRoles: Boolean = false): UserRoleDetail = UserRoleDetail(
  username = this.username,
  activeCaseload = this.activeCaseLoad?.toPrisonCaseload(),
  active = this.staff.isActive,
  accountType = this.type,
  dpsRoles = this.caseloads.filter { uc -> uc.caseload.isDpsCaseload() }
    .map { uc ->
      uc.roles.map {
        it.role.toRoleDetail()
      }
    }.flatten(),
  nomisRoles = if (includeNomisRoles) {
    this.caseloads.filter { uc -> !uc.caseload.isDpsCaseload() }
      .map { uc ->
        CaseloadRoleDetail(
          caseload = uc.caseload.toPrisonCaseload(),
          roles = uc.roles.map {
            it.role.toRoleDetail()
          }
        )
      }
  } else {
    null
  }
)

fun Caseload.toPrisonCaseload(): PrisonCaseload = PrisonCaseload(
  id = this.id,
  name = this.name.capitalizeLeavingAbbreviations()
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
