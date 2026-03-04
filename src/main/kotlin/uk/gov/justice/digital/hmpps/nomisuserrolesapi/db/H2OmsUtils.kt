package uk.gov.justice.digital.hmpps.nomisuserrolesapi.db

import java.sql.Connection
import java.sql.SQLException

class H2OmsUtils {
  companion object {
    @JvmStatic
    @Throws(SQLException::class)
    fun recordLogonDate(conn: Connection, username: String) {
      val sql = "UPDATE staff_user_accounts SET last_logon_date = now() WHERE username = ?"
      conn.prepareStatement(sql).use { stmt ->
        stmt.setString(1, username)
        stmt.executeUpdate()
      }
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun createUser(conn: Connection, username: String, initialPassword: String, profile: String) {
      val createUserSql = "CREATE USER $username PASSWORD ?"
      conn.prepareStatement(createUserSql).use { createStmt ->
        createStmt.setString(1, initialPassword)
        createStmt.executeUpdate()
      }
      val insertSql = "INSERT INTO DBA_USERS (USERNAME, ACCOUNT_STATUS, PROFILE) values (?, ?, ?)"
      conn.prepareStatement(insertSql).use { insertStmt ->
        insertStmt.setString(1, username)
        insertStmt.setString(2, "OPEN")
        insertStmt.setString(3, profile)
        insertStmt.executeUpdate()
      }
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun dropUser(conn: Connection, username: String) {
      val dropUserSql = "DROP USER $username"
      conn.prepareStatement(dropUserSql).use { dropStmt ->
        dropStmt.executeUpdate()
      }
      val deleteSql = "DELETE FROM DBA_USERS where USERNAME = ?"
      conn.prepareStatement(deleteSql).use { deleteStmt ->
        deleteStmt.setString(1, username)
        deleteStmt.executeUpdate()
      }
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun expirePassword(conn: Connection, username: String) {
      val sql = "UPDATE dba_users SET account_status = ? WHERE username = ?"
      conn.prepareStatement(sql).use { stmt ->
        stmt.setString(1, "EXPIRED")
        stmt.setString(2, username)
        stmt.executeUpdate()
      }
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun lockUser(conn: Connection, username: String) {
      val sql = "UPDATE dba_users SET account_status = ? WHERE username = ?"
      conn.prepareStatement(sql).use { stmt ->
        stmt.setString(1, "LOCKED")
        stmt.setString(2, username)
        stmt.executeUpdate()
      }
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun unlockUser(conn: Connection, username: String) {
      val sql = "UPDATE dba_users SET account_status = ? WHERE username = ?"
      conn.prepareStatement(sql).use { stmt ->
        stmt.setString(1, "OPEN")
        stmt.setString(2, username)
        stmt.executeUpdate()
      }
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun changePassword(conn: Connection, username: String, password: String) {
      val changePasswordSql = "ALTER USER $username SET password ?"
      conn.prepareStatement(changePasswordSql).use { stmt ->
        stmt.setString(1, username)
        stmt.executeUpdate()
      }
      val statusSql = "SELECT account_status from dba_users WHERE username = ?"
      conn.prepareStatement(statusSql).use { statusStmt ->
        statusStmt.setString(1, username)
        statusStmt.executeQuery().use { rs ->
          if (rs.next()) {
            val accountStatus = rs.getString(1)
            if ("EXPIRED" == accountStatus) {
              val lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?"
              conn.prepareStatement(lockSql).use { lockStmt ->
                lockStmt.setString(1, "OPEN")
                lockStmt.setString(2, username)
                lockStmt.executeUpdate()
              }
            }
          }
        }
      }
    }
  }
}
