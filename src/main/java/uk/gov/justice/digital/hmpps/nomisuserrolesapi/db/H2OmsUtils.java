package uk.gov.justice.digital.hmpps.nomisuserrolesapi.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class H2OmsUtils {

  private H2OmsUtils() {
  }

  public static void recordLogonDate(Connection conn, String username) throws SQLException {
    final String sql = "UPDATE staff_user_accounts SET last_logon_date = now() WHERE username = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, username);
      stmt.executeUpdate();
    }
  }

  public static void createUser(Connection conn, String username, String initialPassword, String profile) throws SQLException {
    final String createUserSql = String.format("CREATE USER %s PASSWORD ?", username);
    try (PreparedStatement createStmt = conn.prepareStatement(createUserSql)) {
      createStmt.setString(1, initialPassword);
      createStmt.executeUpdate();
    }

    final String insertSql = "INSERT INTO DBA_USERS (USERNAME, ACCOUNT_STATUS, PROFILE) values (?, ?, ?)";
    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
      insertStmt.setString(1, username);
      insertStmt.setString(2, "OPEN");
      insertStmt.setString(3, profile);
      insertStmt.executeUpdate();
    }
  }

  public static void dropUser(Connection conn, String username) throws SQLException {
    final String dropUserSql = String.format("DROP USER %s", username);
    try (PreparedStatement dropStmt = conn.prepareStatement(dropUserSql)) {
      dropStmt.executeUpdate();
    }

    final String deleteSql = "DELETE FROM DBA_USERS where USERNAME = ?";
    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
      deleteStmt.setString(1, username);
      deleteStmt.executeUpdate();
    }
  }

  public static void expirePassword(Connection conn, String username) throws SQLException {
    final String sql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, "EXPIRED");
      stmt.setString(2, username);
      stmt.executeUpdate();
    }
  }

  public static void lockUser(Connection conn, String username) throws SQLException {
    final String sql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, "LOCKED");
      stmt.setString(2, username);
      stmt.executeUpdate();
    }
  }

  public static void unlockUser(Connection conn, String username) throws SQLException {
    final String sql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, "OPEN");
      stmt.setString(2, username);
      stmt.executeUpdate();
    }
  }

  public static void changePassword(Connection conn, String username, String password) throws SQLException {
    final String changePasswordSql = String.format("ALTER USER %s SET password ?", username);
    try (PreparedStatement stmt = conn.prepareStatement(changePasswordSql)) {
      stmt.setString(1, username);
      stmt.executeUpdate();
    }

    final String statusSql = "SELECT account_status from dba_users WHERE username = ?";
    try (PreparedStatement statusStmt = conn.prepareStatement(statusSql)) {
      statusStmt.setString(1, username);
      try (ResultSet rs = statusStmt.executeQuery()) {
        if (rs.next()) {
          final String accountStatus = rs.getString(1);
          if ("EXPIRED".equals(accountStatus)) {
            final String lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
              lockStmt.setString(1, "OPEN");
              lockStmt.setString(2, username);
              lockStmt.executeUpdate();
            }
          }
        }
      }
    }
  }
}
