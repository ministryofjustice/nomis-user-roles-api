package uk.gov.justice.digital.hmpps.nomisuserrolesapi.batchprocessing

import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.item.ItemStreamException
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.support.ListItemReader
import java.io.Closeable

open class StreamingCartesianReader(
  private val userIdsReader: ItemStreamReader<String>,
  private val rolesReader: ListItemReader<String>,
) : ItemStreamReader<UserRoleAssignment>, Closeable {

  private val contextKeyPrefix: String = "streamCartesian"
  private val outerKey = "$contextKeyPrefix.outerValue"
  private val rolesIndexKey = "$contextKeyPrefix.rolesIndex"
  private val rolesList = mutableListOf<String>()

  private var opened = false
  private var rolesIndex = 0
  private var currentUserId: String? = null

  override fun open(executionContext: ExecutionContext) {
    if (opened) return

    userIdsReader.open(executionContext)

    rolesLoop@ while (true) {
      val r = rolesReader.read() ?: break@rolesLoop
      rolesList.add(r)
    }

    rolesIndex = if (executionContext.containsKey(outerKey)) executionContext.getInt(rolesIndexKey) else 0

    if (currentUserId == null) currentUserId = userIdsReader.read()

    opened = true
  }

  override fun read(): UserRoleAssignment? {
    if (!opened) throw ItemStreamException("Streaming cartesian must be open before read()")

    if (currentUserId == null || rolesList.isEmpty()) return null

    // Move to next user and reset the roles index to 0
    if (rolesIndex >= rolesList.size) {
      currentUserId = userIdsReader.read()
      rolesIndex = 0
    }

    return currentUserId?.let { UserRoleAssignment(currentUserId!!, rolesList[rolesIndex]).also { rolesIndex++ } }
  }

  override fun update(executionContext: ExecutionContext) {
    currentUserId?.let {
      executionContext.putString(outerKey, currentUserId)
      executionContext.putString(rolesIndexKey, rolesIndex.toString())
    }
  }

  override fun close() {
    this.userIdsReader.close()
    this.opened = false
  }
}
