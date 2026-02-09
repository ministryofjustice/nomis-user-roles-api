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
  @Qualifier("batchAddRolesToUsersJob") private val job2: Job,
) {

  data class AddRolesToUsersInstance(
    val jobName: String,
    val jobId: Long,
    val status: String,
    val rolesList: List<String>,
  )

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/list-jobs")
  fun getAddRolesToUsersJobs(): ResponseEntity<List<AddRolesToUsersInstance>> {
    val results: MutableList<AddRolesToUsersInstance> = mutableListOf()

    jobRepository.getJobInstances("batchAddRolesToUsersJob", 0, 100)
      .forEach { inst ->
        results.addAll(
          jobRepository.getJobExecutions(inst).map { execution ->
            AddRolesToUsersInstance(
              inst.jobName,
              inst.instanceId,
              execution.status.name,
              execution.jobParameters.getRolesList(),
            )
          },
        )
      }

    return ResponseEntity.status(200).body(results)
  }

  data class Body(val userIds: List<String>?, val roles: List<String>)

  @PostMapping
  fun bob(
    @RequestBody body: Body,
  ): ResponseEntity<String> {

    val execution = jobOperator.start(
      job2,
      JobParametersBuilder()
        .addString("rolesList", body.roles.joinToString(","))
        .addString("uuid", UUID.randomUUID().toString())
        .toJobParameters(),
    )
    log.info("Bulk add roles to users job started with ID: {},{},{}", execution.id, execution.status, execution.endTime)
    return ResponseEntity.status(200).body("Bulk add roles to users job started with ID: ${execution.id}")
  }

  fun JobParameters.getRolesList(): List<String> = this.getParameter("rolesList")
    ?.takeIf { it.type == String::class.java }
    ?.let { list -> list.value.toString().split(",").map { it.trim() } }
    ?: emptyList()
}
