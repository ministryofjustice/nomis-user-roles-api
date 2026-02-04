package uk.gov.justice.digital.hmpps.nomisuserrolesapi.config

import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.nomisuserrolesapi.service.UserRoleAlreadyExistsException

@Configuration
class AddRolesToUsersBatchProcessingConfiguration {

  companion object {
    private val log = LoggerFactory.getLogger(AddRolesToUsersBatchProcessingConfiguration::class.java)
  }

  data class UserIdCsv(val userId: String)

  data class UserRoleAssignment(
    val userId: String,
    val role: String,
  )

  @Bean
  fun userIdReader(): FlatFileItemReader<UserIdCsv> =
    FlatFileItemReaderBuilder<UserIdCsv>()
      .name("userIdCsvReader")
      .resource(ClassPathResource("example-persons.csv"))
      .delimited()
      .names("firstName", "surname").fieldSetMapper { fs ->
        UserIdCsv(fs.readString(0) ?: "")
      }
      .saveState(true)
      .build()

  @Bean
  @StepScope
  fun userRoleProcessor(
    @Value("#{jobParameters['roles']}") rolesParams: String,
  ): ItemProcessor<UserIdCsv, List<UserRoleAssignment>> {
    val roles = rolesParams.split(",")
      .filter { it.isNotBlank() }

    return ItemProcessor { item ->
      roles.map { role ->
        UserRoleAssignment(item.userId, role)
      }
    }
  }

  @Bean
  fun userRoleWriter(): ItemWriter<List<UserRoleAssignment>> = ItemWriter { chunk ->
    chunk.forEach { chunk ->
      chunk.forEach { assignment ->
        log.info("Writing user role assignment for ${assignment.userId}=${assignment.role}")

        if (assignment.userId == "Jane" && assignment.role == "ROLE1") {
          throw UserRoleAlreadyExistsException("USER ${assignment.userId} is already assigned role: ${assignment.role}")
        }

        log.info("success")
      }
    }
  }

  @Bean("batchAddRolesToUsersStep")
  fun userRoleAssignmentStep(
    jobRepository: JobRepository,
    reader: FlatFileItemReader<UserIdCsv>,
    processor: ItemProcessor<UserIdCsv, List<UserRoleAssignment>>,
    writer: ItemWriter<List<UserRoleAssignment>>,
  ): Step {

    return StepBuilder("userRoleAssignmentStep", jobRepository)
      .chunk<UserIdCsv, List<UserRoleAssignment>>(1)
      .reader(reader)
      .processor(processor)
      .writer(writer)
      .faultTolerant()
      .skip(UserRoleAlreadyExistsException::class.java)
      .skipLimit(10)
      .build()
  }

  @Bean("batchAddRolesToUsersJob")
  fun batchAddRolesToUsersJob(
    jobRepository: JobRepository,
    @Qualifier("batchAddRolesToUsersStep") step: Step,
  ): Job = JobBuilder("batchAddRolesToUsersJob", jobRepository)
    .start(step)
    .build()
}
