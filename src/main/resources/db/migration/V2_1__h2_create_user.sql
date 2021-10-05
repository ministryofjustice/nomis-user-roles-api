CREATE SCHEMA IF NOT EXISTS oms_utils;
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
