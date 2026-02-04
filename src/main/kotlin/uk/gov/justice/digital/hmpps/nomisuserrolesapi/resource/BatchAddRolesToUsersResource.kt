package uk.gov.justice.digital.hmpps.nomisuserrolesapi.resource

import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ROLE_MAINTAIN_ACCESS_ROLES_ADMIN', 'ROLE_MAINTAIN_ACCESS_ROLES')")
@RequestMapping("/batch/add-roles-to-users", produces = [MediaType.APPLICATION_JSON_VALUE])
class BatchAddRolesToUsersResource(
  private val jobOperator: JobOperator,
  private val jobRepository: JobRepository,
  @Qualifier("batchAddRolesToUsersJob") private val job: Job,
) {

  data class AddRolesToUsersInstance(val jobName: String, val status: String, val params: JobParameters)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/instances")
  fun getAddRolesToUsersJobs(): ResponseEntity<List<AddRolesToUsersInstance>> {
    val results: MutableList<AddRolesToUsersInstance> = mutableListOf()

    jobRepository.getJobInstances("batchAddRolesToUsersJob", 0, 20).forEach { instance ->
      results.addAll(
        instance.jobExecutions.map { execution ->
          AddRolesToUsersInstance(instance.jobName, execution.status.name, execution.jobParameters)
        },
      )
    }

    return ResponseEntity.status(200).body(results)
  }

  @PostMapping
  fun bulkAddRolesToUsers(
    @RequestBody roles: List<String>,
  ): ResponseEntity<String> {
    log.info("Bulk add roles to users: {}", roles)

    val params = JobParametersBuilder()
      .addString("roles", roles.joinToString(","))
      .addString("internal-uuid", UUID.randomUUID().toString())
      .toJobParameters()

    val execution = jobOperator.start(job, params)
    log.info("Bulk add roles to users job started with ID: {}", execution.id)

    return ResponseEntity.status(200).body("Bulk add roles to users job started with ID: ${execution.id}")
  }
}