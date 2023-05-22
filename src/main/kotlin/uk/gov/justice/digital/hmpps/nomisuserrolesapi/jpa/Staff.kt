package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.apache.commons.text.WordUtils
import org.hibernate.Hibernate
import org.hibernate.annotations.Where

@Entity
@Table(name = "STAFF_MEMBERS")
data class Staff(
  @Id
  @SequenceGenerator(name = "STAFF_ID", sequenceName = "STAFF_ID", allocationSize = 1)
  @GeneratedValue(generator = "STAFF_ID")
  @Column(name = "STAFF_ID", nullable = false)
  var staffId: Long = 0,

  @Column(name = "FIRST_NAME", nullable = false)
  var firstName: String,

  @Column(name = "LAST_NAME", nullable = false)
  var lastName: String,

  @Column(name = "STATUS")
  var status: String,

  @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  var users: List<UserPersonDetail> = listOf(),

  @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @Where(clause = "OWNER_CLASS = 'STF' AND INTERNET_ADDRESS_CLASS = 'EMAIL'")
  val emails: MutableSet<EmailAddress> = mutableSetOf(),
) {

  fun fullName(): String = "$firstName $lastName".capitalizeFully()

  fun generalAccount(): UserPersonDetail? = users.firstOrNull { u -> UsageType.GENERAL == u.type }

  fun adminAccount(): UserPersonDetail? = users.firstOrNull { u -> UsageType.ADMIN == u.type }

  fun primaryEmail(): EmailAddress? =
    emails.firstOrNull { e -> e.emailCaseSensitive.contains("justice.gov.uk") } ?: run { emails.firstOrNull() }

  val isActive: Boolean
    get() = STAFF_STATUS_ACTIVE == status

  companion object {
    internal const val STAFF_STATUS_ACTIVE = "ACTIVE"
    internal const val STAFF_STATUS_INACTIVE = "INACT"
  }

  fun setEmail(email: String) {
    emails.clear()
    emails.add(EmailAddress(emailCaseSensitive = email, staff = this))
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Staff

    return staffId == other.staffId
  }

  override fun hashCode(): Int = staffId.hashCode()

  @Override
  override fun toString(): String {
    return this::class.simpleName + "(staffId = $staffId )"
  }
}

fun String.capitalizeFully(): String = WordUtils.capitalizeFully(this)
