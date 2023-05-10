package uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

class UpperCaseNamingStrategy : PhysicalNamingStrategyStandardImpl() {
  override fun toPhysicalColumnName(logicalName: Identifier, context: JdbcEnvironment): Identifier {
    return Identifier(logicalName.text.uppercase(), logicalName.isQuoted)
  }
}
