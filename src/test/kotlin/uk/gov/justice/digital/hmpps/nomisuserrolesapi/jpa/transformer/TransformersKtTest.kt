package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.transformer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.data.PrisonCaseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Caseload
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.Staff
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UsageType
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.UserPersonDetail

internal class TransformersKtTest {

  @Nested
  @DisplayName("toUserSummary")
  inner class ToUserSummary {
    @Test
    fun `will copy summary data`() {
      val entity = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(staffId = 99, firstName = "RAJ", lastName = "MAKI", status = "ACTIVE"),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH (HMP)")
      )

      val data = entity.toUserSummary()

      assertThat(data.username).isEqualTo("raj.maki")
      assertThat(data.active).isTrue
      assertThat(data.firstName).isEqualTo("Raj")
      assertThat(data.lastName).isEqualTo("Maki")
      assertThat(data.staffId).isEqualTo(99)
      assertThat(data.activeCaseload).isEqualTo(PrisonCaseload("WWI", "WANDSWORTH (HMP)"))
    }
    @Test
    fun `will copy names and capitalize`() {
      val entity = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(staffId = 99, firstName = "RAJ BOB", lastName = "MAKI", status = "ACTIVE"),
        type = UsageType.GENERAL,
        activeCaseLoad = Caseload("WWI", "WANDSWORTH (HMP)")
      )

      val data = entity.toUserSummary()

      assertThat(data.firstName).isEqualTo("Raj Bob")
      assertThat(data.lastName).isEqualTo("Maki")
    }

    @Test
    @DisplayName("will not attempt to copy active caseload if not set")
    internal fun `will not attempt to copy active caseload if not set`() {
      val entity = UserPersonDetail(
        username = "raj.maki",
        staff = Staff(staffId = 99, firstName = "Raj", lastName = "Maki", status = "ACTIVE"),
        type = UsageType.GENERAL
      )

      val data = entity.toUserSummary()

      assertThat(data.activeCaseload).isNull()
    }
  }
}
