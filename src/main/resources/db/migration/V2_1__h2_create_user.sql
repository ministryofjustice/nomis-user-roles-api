CREATE SCHEMA IF NOT EXISTS oms_utils;

CREATE ALIAS oms_utils.record_logon_date AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void recordLogonDate(Connection conn, String username) throws SQLException {
    final var recordLogonDateSql = "UPDATE staff_user_accounts SET last_logon_date = now() WHERE username = ?";
    final var statement = conn.prepareStatement(recordLogonDateSql);
    statement.setString(1, username);
    statement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.create_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void createUser(Connection conn, String username, String initialPassword, String profile) throws SQLException {
    final var createUserSql = String.format("CREATE USER %s PASSWORD ?", username);
    var statement = conn.prepareStatement(createUserSql);
    statement.setString(1, initialPassword);
    statement.executeUpdate();
    statement = conn.prepareStatement("INSERT INTO DBA_USERS (USERNAME, ACCOUNT_STATUS, PROFILE) values (?, ?, ?)");
    statement.setString(1, username);
    statement.setString(2, "OPEN");
    statement.setString(3, profile);
    statement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.drop_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void dropUser(Connection conn, String username) throws SQLException {
    final var dropUserSql = String.format("DROP USER %s", username);
    var statement = conn.prepareStatement(dropUserSql);
    statement.executeUpdate();
    statement = conn.prepareStatement("DELETE FROM DBA_USERS where USERNAME = ?");
    statement.setString(1, username);
    statement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.expire_password AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void expirePassword(Connection conn, String username) throws SQLException {
    final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    final var statement = conn.prepareStatement(lockSql);
    statement.setString(1, "EXPIRED");
    statement.setString(2, username);
    statement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.lock_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void lockUser(Connection conn, String username) throws SQLException {
    final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    final var statement = conn.prepareStatement(lockSql);
    statement.setString(1, "LOCKED");
    statement.setString(2, username);
    statement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.unlock_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void unlockUser(Connection conn, String username) throws SQLException {
    final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
    final var lockStatement = conn.prepareStatement(lockSql);
    lockStatement.setString(1, "OPEN");
    lockStatement.setString(2, username);
    lockStatement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.change_user_password AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void changePassword(Connection conn, String username, String password) throws SQLException {
    final var changePasswordSql = String.format("ALTER USER %s SET password ?", username);
    final var statement = conn.prepareStatement(changePasswordSql);
    statement.setString(1, username);
    statement.executeUpdate();

    final var statusSql = "SELECT account_status from dba_users WHERE username = ?";
    final var statusStatement = conn.prepareStatement(statusSql);
    statusStatement.setString(1, username);
    final var resultSet = statusStatement.executeQuery();
    if (resultSet.next()) {
        final var accountStatus = resultSet.getString(1);
        if (accountStatus.equals("EXPIRED")) {
            final var lockSql = "UPDATE dba_users SET account_status = ? WHERE username = ?";
            final var lockStatement = conn.prepareStatement(lockSql);
            lockStatement.setString(1, "OPEN");
            lockStatement.setString(2, username);
            lockStatement.executeUpdate();
        }
    }
    resultSet.close();
}
$$;
