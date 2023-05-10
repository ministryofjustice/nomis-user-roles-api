package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.Hibernate

@Entity
@Table(name = "INTERNET_ADDRESSES")
data class EmailAddress(

  @Id
  @SequenceGenerator(name = "INTERNET_ADDRESS_ID", sequenceName = "INTERNET_ADDRESS_ID", allocationSize = 1)
  @GeneratedValue(generator = "INTERNET_ADDRESS_ID")
  @Column(name = "INTERNET_ADDRESS_ID", nullable = false)
  val id: Long = 0,

  @Column(name = "OWNER_CLASS", nullable = false)
  val userType: String = "STF",

  @Column(name = "INTERNET_ADDRESS_CLASS", nullable = false)
  val type: String = "EMAIL",

  @Column(name = "INTERNET_ADDRESS")
  val email: String,

  @ManyToOne(optional = false)
  @JoinColumn(name = "OWNER_ID", nullable = false)
  val staff: Staff,

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as EmailAddress

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
