CREATE SCHEMA IF NOT EXISTS oms_utils;
CREATE ALIAS oms_utils.create_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void createUser(Connection conn, String username, String initialPassword, String userProfile) throws SQLException {
    final var createUserSql = String.format("CREATE USER %s PASSWORD ?", username);
    final var statement = conn.prepareStatement(createUserSql);
    statement.setString(1, initialPassword);
    statement.executeUpdate();
}
$$;

CREATE ALIAS oms_utils.drop_user AS $$
import java.sql.Connection;
import java.sql.SQLException;
@CODE
void dropUser(Connection conn, String username) throws SQLException {
    final var dropUserSql = String.format("DROP USER %s", username);
    final var statement = conn.prepareStatement(dropUserSql);
    statement.executeUpdate();
}
$$;