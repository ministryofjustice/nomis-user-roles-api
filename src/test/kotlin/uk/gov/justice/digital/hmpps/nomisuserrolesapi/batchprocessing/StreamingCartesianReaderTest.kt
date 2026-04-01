package uk.gov.justice.digital.hmpps.nomisuserrolesapi.batchprocessing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.infrastructure.item.support.ListItemReader
import org.springframework.core.io.ClassPathResource

class StreamingCartesianReaderTest {

  @Test
  fun `should produce all expected combinations of UserID and RoleId`() {
    val expected: List<UserRoleAssignment> = listOf(
      UserRoleAssignment("AAA666", "ROLE1"),
      UserRoleAssignment("AAA666", "ROLE2"),
      UserRoleAssignment("AAA666", "ROLE3"),
      UserRoleAssignment("BBB777", "ROLE1"),
      UserRoleAssignment("BBB777", "ROLE2"),
      UserRoleAssignment("BBB777", "ROLE3"),
      UserRoleAssignment("CCC112", "ROLE1"),
      UserRoleAssignment("CCC112", "ROLE2"),
      UserRoleAssignment("CCC112", "ROLE3"),
    )

    val reader = StreamingCartesianReader(
      userIdsReader = getCsvItemStreamReader(),
      rolesReader = getRolesListItemReader(),
    )

    val actual = reader.readStreamIntoList()
    assertThat(actual).containsExactlyElementsOf(expected)
  }

  private fun StreamingCartesianReader.readStreamIntoList(): List<UserRoleAssignment> = this.use { _ ->
    val results: MutableList<UserRoleAssignment> = mutableListOf()
    this.open(ExecutionContext())

    loop@ while (true) {
      this.read()?.let { results.add(it) } ?: break@loop
    }
    results
  }

  private fun getRolesListItemReader(): ListItemReader<String> = ListItemReader<String>(
    listOf(
      "ROLE1",
      "ROLE2",
      "ROLE3",
    ),
  )

  private fun getCsvItemStreamReader(): ItemStreamReader<String> = FlatFileItemReaderBuilder<String>()
    .name("user-ids")
    .resource(ClassPathResource("test-userids.csv"))
    .delimited()
    .names("id")
    .fieldSetMapper { fs -> fs.readString(0) ?: "" }
    .build()
}