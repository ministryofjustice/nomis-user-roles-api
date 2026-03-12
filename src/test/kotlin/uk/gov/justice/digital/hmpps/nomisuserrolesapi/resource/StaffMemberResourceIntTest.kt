package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.JobClassificationRequest
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.StaffJobClassification
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceData
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataDomain
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataDomain.STAFF_ROLE
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataKey
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRole
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.StaffLocationRoleRepository
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserCaseloadPk
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.of
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.StaffRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDate.now

class StaffMemberResourceIntTest : IntegrationTestBase() {

  @Autowired
  internal lateinit var staffRepository: StaffRepository

  @Autowired
  internal lateinit var referenceDataRepository: ReferenceDataRepository

  @Autowired
  internal lateinit var staffLocationRoleRepository: StaffLocationRoleRepository

  @BeforeEach
  fun setup() {
    staffLocationRoleRepository.deleteAll()
    staffRepository.deleteAll()
    referenceDataRepository.deleteAll()
  }

  @AfterEach
  fun clearDown() {
    staffLocationRoleRepository.deleteAll()
    staffRepository.deleteAll()
    referenceDataRepository.deleteAll()
  }

  @Test
  fun `access forbidden when no role`() {
    setStaffMemberRole(1, roles = listOf())
      .expectStatus().isForbidden
  }

  @Test
  fun `access forbidden with wrong role`() {
    setStaffMemberRole(1, roles = listOf("ROLE_NE_OTHER__RW"))
      .expectStatus().isForbidden
  }

  @Test
  fun `staff member not found`() {
    setStaffMemberRole(-1).expectStatus().isNotFound
  }

  @ParameterizedTest
  @MethodSource("invalidJobClassificationRequests")
  fun `validation of job classification`(request: JobClassificationRequest, message: String) {
    val res: ErrorResponse = setStaffMemberRole(29, jobClassification = request)
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

    assertThat(res.userMessage).isEqualTo(message)
  }

  @Test
  fun `staff not at agency`() {
    val staff = givenStaffMember()
    val res = setStaffMemberRole(staff.staffId)
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

    assertThat(res.userMessage).isEqualTo("Validation failure: Staff not at agency")
  }

  @Test
  fun `staff role not found`() {
    val staff = givenStaffMember(staff().withUser())
    val res = setStaffMemberRole(staff.staffId)
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

    assertThat(res.userMessage).isEqualTo("Validation failure: Staff role not found")
  }

  @Test
  fun `staff position not found`() {
    val staff = givenStaffMember(staff().withUser())
    givenReferenceData(referenceData(STAFF_ROLE of "KW"))
    val res = setStaffMemberRole(staff.staffId)
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

    assertThat(res.userMessage).isEqualTo("Validation failure: Staff position not found")
  }

  @Test
  fun `staff schedule type not found`() {
    val staff = givenStaffMember(staff().withUser())
    givenReferenceData(referenceData(STAFF_ROLE of "KW"))
    givenReferenceData(referenceData(ReferenceDataDomain.STAFF_POS of "POS"))
    val res = setStaffMemberRole(staff.staffId)
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

    assertThat(res.userMessage).isEqualTo("Validation failure: Schedule type not found")
  }

  @Test
  fun `existing staff role updated with new values`() {
    val staff = givenStaffMember(staff().withUser())
    val jobClassification = jobClassification(toDate = now())
    val role = givenReferenceData(referenceData(STAFF_ROLE of "KW"))
    val position = givenReferenceData(referenceData(ReferenceDataDomain.STAFF_POS of jobClassification.position))
    val scheduleType =
      givenReferenceData(referenceData(ReferenceDataDomain.SCHEDULE_TYP of jobClassification.scheduleType))
    givenStaffLocationRole(
      "LEI",
      staff.staffId,
      role.code,
      jobClassification.fromDate,
      null,
      position.code,
      scheduleType,
      BigDecimal(40),
    )

    val res = setStaffMemberRole(staff.staffId, role.code, jobClassification)
      .expectStatus().isOk
      .expectBody(StaffJobClassification::class.java).returnResult().responseBody!!

    res.verifyAgainst("LEI", staff.staffId, role.code, jobClassification)
    val slr = staffLocationRoleRepository.findAllByAgencyIdAndStaffIdAndRole("LEI", staff.staffId, role.code)
    assertThat(slr).hasSize(1)
    assertThat(slr.first().toDate).isEqualTo(now())
  }

  @Test
  fun `new role created with new values`() {
    val staff = givenStaffMember(staff().withUser())
    val jobClassification = jobClassification(fromDate = now().minusDays(7))
    val role = givenReferenceData(referenceData(STAFF_ROLE of "KW"))
    givenReferenceData(referenceData(ReferenceDataDomain.STAFF_POS of jobClassification.position))
    givenReferenceData(referenceData(ReferenceDataDomain.SCHEDULE_TYP of jobClassification.scheduleType))

    val res = setStaffMemberRole(staff.staffId, role.code, jobClassification)
      .expectStatus().isOk
      .expectBody(StaffJobClassification::class.java).returnResult().responseBody!!

    res.verifyAgainst("LEI", staff.staffId, role.code, jobClassification)
    val slr = staffLocationRoleRepository.findAllByAgencyIdAndStaffIdAndRole("LEI", staff.staffId, role.code)
    assertThat(slr).hasSize(1)
    assertThat(slr.first().toDate).isNull()
  }

  @Test
  fun `existing staff roles expired when dates not matching and new role created with new values`() {
    val staff = givenStaffMember(staff().withUser())
    val jobClassification = jobClassification(fromDate = now().minusDays(21))
    val role = givenReferenceData(referenceData(STAFF_ROLE of "KW"))
    val position = givenReferenceData(referenceData(ReferenceDataDomain.STAFF_POS of jobClassification.position))
    val scheduleType =
      givenReferenceData(referenceData(ReferenceDataDomain.SCHEDULE_TYP of jobClassification.scheduleType))
    givenStaffLocationRole(
      "LEI",
      staff.staffId,
      role.code,
      now().minusDays(7),
      null,
      position.code,
      scheduleType,
      BigDecimal(40),
    )
    givenStaffLocationRole(
      "LEI",
      staff.staffId,
      role.code,
      now().minusDays(14),
      now().minusDays(7),
      position.code,
      scheduleType,
      BigDecimal(40),
    )

    val res = setStaffMemberRole(staff.staffId, role.code, jobClassification)
      .expectStatus().isOk
      .expectBody(StaffJobClassification::class.java).returnResult().responseBody!!

    res.verifyAgainst("LEI", staff.staffId, role.code, jobClassification)

    val slr = staffLocationRoleRepository.findAllByAgencyIdAndStaffIdAndRole("LEI", staff.staffId, role.code)
    assertThat(slr).hasSize(3)
    assertThat(slr.count { it.toDate != null }).isEqualTo(2)
    assertThat(slr.count { it.toDate == now() }).isEqualTo(1)
    assertThat(slr.count { it.toDate == null }).isEqualTo(1)
  }

  private fun setStaffMemberRole(
    staffId: Long,
    staffRole: String = "KW",
    jobClassification: JobClassificationRequest = jobClassification(),
    roles: List<String> = listOf("ROLE_NOMIS_MANAGE_USERS__STAFF_MEMBERS__RW"),
  ) = webTestClient.put()
    .uri(STAFF_MEMBER_ROLE_URL, "LEI", staffId, staffRole)
    .bodyValue(jobClassification)
    .headers(setAuthorisation(roles = roles))
    .exchange()

  private fun staff() = Staff(firstName = "John", lastName = "Smith", status = "ACTIVE", staffId = 0)
  private fun givenStaffMember(staff: Staff = staff()) = staffRepository.save(staff)

  private fun caseload(caseloadId: String) = Caseload(caseloadId, "name for $caseloadId")

  private fun Staff.withUser(username: String = "An07h3R", agencyId: String = "LEI") = apply {
    users = listOf(
      UserPersonDetail(username, this, type = UsageType.GENERAL)
        .withCaseload(caseload(agencyId)),
    )
  }

  private fun UserPersonDetail.withCaseload(caseload: Caseload) = apply {
    caseloads.add(UserCaseload(UserCaseloadPk(caseload.id, username), caseload, this, now().minusDays(7)))
  }

  private fun referenceData(key: ReferenceDataKey, active: Boolean = true) = ReferenceData(key, "Description of ${key.code}", active)

  private fun givenReferenceData(rd: ReferenceData) = referenceDataRepository.save(rd)

  private fun StaffJobClassification.verifyAgainst(
    agencyId: String,
    staffId: Long,
    role: String,
    request: JobClassificationRequest,
  ) {
    assertThat(this.agencyId).isEqualTo(agencyId)
    assertThat(this.staffId).isEqualTo(staffId)
    assertThat(this.role).isEqualTo(role)
    assertThat(position).isEqualTo(request.position)
    assertThat(scheduleType).isEqualTo(request.scheduleType)
    assertThat(hoursPerWeek).isEqualTo(request.hoursPerWeek)
  }

  private fun givenStaffLocationRole(
    agencyId: String,
    staffId: Long,
    role: String,
    fromDate: LocalDate,
    toDate: LocalDate?,
    position: String,
    scheduleType: ReferenceData,
    hoursPerWeek: BigDecimal,
  ) = staffLocationRoleRepository.save(
    StaffLocationRole(agencyId, staffId, fromDate, position, role)
      .apply { modify(toDate, scheduleType, hoursPerWeek) },
  )

  companion object {
    const val STAFF_MEMBER_ROLE_URL = "/agency/{agencyId}/staff-members/{staffId}/staff-role/{staffRole}"

    private fun jobClassification(
      position: String = "POS",
      scheduleType: String = "FT",
      hoursPerWeek: BigDecimal = BigDecimal(37.5),
      fromDate: LocalDate = now().minusDays(7),
      toDate: LocalDate? = null,
    ) = JobClassificationRequest(position, scheduleType, hoursPerWeek, fromDate, toDate)

    @JvmStatic
    fun invalidJobClassificationRequests() = listOf(
      Arguments.of(
        jobClassification(hoursPerWeek = BigDecimal(-1.0)),
        "Validation failure: hours per week must be greater than zero",
      ),
      Arguments.of(
        jobClassification(hoursPerWeek = BigDecimal(37.585)),
        "Validation failure: hours per week must match nnnn.nn",
      ),
      Arguments.of(
        jobClassification(fromDate = now().plusDays(1)),
        "Validation failure: from date must be today or in the past",
      ),
      Arguments.of(
        jobClassification(toDate = now().plusDays(1)),
        "Validation failure: to date must be null, today or in the past",
      ),
      Arguments.of(jobClassification(scheduleType = ""), "Validation failure: schedule type must not be blank"),
      Arguments.of(jobClassification(position = ""), "Validation failure: position must not be blank"),
    )
  }
}
