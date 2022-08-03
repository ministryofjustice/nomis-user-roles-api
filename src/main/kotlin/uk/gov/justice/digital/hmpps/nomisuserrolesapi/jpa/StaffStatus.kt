package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

enum class StaffStatus(
  val code: Int,
  val desc: String,
) {
  Active(0, "Active"),
  SUS(1, "SUS"),
  INACT(2, "INACT"),
  CAREER(2, "CAREER"),
  MAT(4, "MAT"),
  ACTIVE(5, "ACTIVE"),
  SAB(6, "SAB"),
  SICK(7, "SICK");

  companion object {
    fun get(code: Int): StaffStatus = values().first { it.code == code }

    fun get(desc: String): StaffStatus = values().first { it.desc == desc }
  }
}
