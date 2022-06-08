package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.Hibernate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table

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

  @ManyToOne(cascade = [CascadeType.ALL], optional = false)
  @JoinColumn(name = "OWNER_ID", nullable = false)
  val staff: Staff

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as EmailAddress

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
