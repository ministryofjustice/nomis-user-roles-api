package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.SecondaryTable
import javax.persistence.Table

@Entity
@Table(name = "STAFF_USER_ACCOUNTS")
@SecondaryTable(name = "SYS.USER$", pkJoinColumns = [PrimaryKeyJoinColumn(name = "NAME")])
class NomisUserPersonDetails(
  @Id
  @Column(name = "USERNAME", nullable = false)
  private val username: String,
  @OneToOne(cascade = [CascadeType.ALL])
  @PrimaryKeyJoinColumn
  val accountDetail: AccountDetail,
  @ManyToOne
  @JoinColumn(name = "STAFF_ID")
  val staff: Staff,
  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "USERNAME")
  val roles: List<UserCaseloadRole> = listOf()
) {

  @Column(name = "SPARE4", table = "SYS.USER$")
  private var password: String? = null

  @Column(name = "STAFF_USER_TYPE", nullable = false)
  var type: String? = null

  @Column(name = "WORKING_CASELOAD_ID")
  var activeCaseLoadId: String? = null

  constructor(
    username: String,
    password: String?,
    staff: Staff,
    type: String?,
    activeCaseLoadId: String?,
    roles: List<UserCaseloadRole>,
    accountDetail: AccountDetail
  ) : this(username, accountDetail, staff, roles) {
    this.password = password
    this.type = type
    this.activeCaseLoadId = activeCaseLoadId
  }
}
