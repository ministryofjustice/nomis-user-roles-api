package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.infrastructure.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.batchprocessing.StreamingCartesianReader
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.batchprocessing.UserRoleAssignment
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserRoleNotFoundException


@Configuration
@EnableJdbcJobRepository(tablePrefix = "BATCH_")
class AddRolesToUsersBatchConfiguration {

  companion object {
    private val log = LoggerFactory.getLogger(AddRolesToUsersBatchConfiguration::class.java)
  }

  @Bean("userIdReader")
  fun userIdReader(): FlatFileItemReader<String> =
    FlatFileItemReaderBuilder<String>()
      .name("userIdCsvReader")
      .resource(ClassPathResource("example-persons.csv"))
      .delimited()
      .names("firstName").fieldSetMapper { fs -> fs.readString(0) ?: "" }
      .saveState(true)
      .build()

  @Bean("streamingCartesianReader")
  @StepScope
  fun userIdToRoleCartesianReader(
    @Value("#{jobParameters['rolesList']}") roles: List<String>,
    @Qualifier("userIdReader") userIdReader: ItemStreamReader<String>,
  ): StreamingCartesianReader? = StreamingCartesianReader(
    userIdsReader = userIdReader,
    rolesReader = ListItemReader(roles),
  )

  @Bean("userRoleWriter")
  fun userRoleWriterV2(): ItemWriter<UserRoleAssignment> = ItemWriter { chunk ->
    chunk.forEach { assignment ->
      log.info("writing user role assignment for ${assignment.userId}=${assignment.role}")
    }
  }

  @Bean("batchAddRolesToUsersStep")
  fun userRolesStepV2(
    jobRepository: JobRepository,
    reader: StreamingCartesianReader,
    writer: ItemWriter<UserRoleAssignment>,
  ): Step = StepBuilder("userRoleAssignmentStep", jobRepository)
    .chunk<UserRoleAssignment, UserRoleAssignment>(1)
    .reader(reader)
    .faultTolerant()
    .skip(UserRoleNotFoundException::class.java)
    .skipLimit(Long.MAX_VALUE)
    .writer(writer)
    .build()

  @Bean("batchAddRolesToUsersJob")
  fun batchAddRolesToUsersJob(
    jobRepository: JobRepository,
    @Qualifier("batchAddRolesToUsersStep") step: Step,
  ): Job = JobBuilder("batchAddRolesToUsersJob", jobRepository)
    .start(step)
    .build()
}