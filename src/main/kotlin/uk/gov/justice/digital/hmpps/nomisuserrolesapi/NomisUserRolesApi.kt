package uk.gov.justice.digital.hmpps.nomisuserrolesapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class NomisUserRolesApi

fun main(args: Array<String>) {
  runApplication<NomisUserRolesApi>(*args)
}
