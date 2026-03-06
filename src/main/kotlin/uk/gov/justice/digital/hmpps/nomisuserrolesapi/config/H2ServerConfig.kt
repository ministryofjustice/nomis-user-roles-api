package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.h2.tools.Server
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.sql.SQLException
import javax.sql.DataSource

@Configuration
@Profile("dev")
class H2ServerConfig {
  @Bean(name = [H2_BEAN_NAME], initMethod = "start", destroyMethod = "stop")
  @Throws(SQLException::class)
  fun h2TcpServer(): Server = Server.createTcpServer(
    "-tcp",
    "-tcpAllowOthers",
    "-tcpPort",
    "9092",
    // uncomment to enable H2 tracing
    // "-trace",
    // required to allow connections to create tables
    "-ifNotExists",
  )

  @Bean
  fun forceDataSourceDependsOnH2() = EnsureBeansOfTypeDependOnBeanWithName(
    DataSource::class.java,
    H2_BEAN_NAME,
  )

  class EnsureBeansOfTypeDependOnBeanWithName(
    val beanTypeToModify: Class<*>,
    val beanNameToDependOn: String,
  ) : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
      val names = beanFactory.getBeanNamesForType(beanTypeToModify, true, false)
      for (name in names) {
        val bd = beanFactory.getBeanDefinition(name)
        val newDependsOn = when (val existing = bd.dependsOn) {
          null -> arrayOf(beanNameToDependOn)
          else -> (existing.asList() + beanNameToDependOn).toTypedArray()
        }
        bd.setDependsOn(*newDependsOn)
      }
    }
  }

  companion object {
    private const val H2_BEAN_NAME = "h2TcpServer"
  }
}
