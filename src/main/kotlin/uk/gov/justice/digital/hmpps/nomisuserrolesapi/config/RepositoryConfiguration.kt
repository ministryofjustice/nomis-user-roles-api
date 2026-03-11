package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
class RepositoryConfiguration {

    @EnableJpaRepositories(
        basePackages = ["uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.entitygraph"],
        repositoryFactoryBeanClass = com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean::class,
        )
    class EntityGraphRepositoryConfiguration

    @EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.nomisuserrolesapi.jpa.repository.standard"])
    class StandardRepositoryConfiguration
}