package dz.usthb.eclipseworkspace.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/eclipseworkspace";
    private static final String USER = "postgres";
    private static final String PASSWORD = "amira";

    private DBConnection() {} // prevent instantiation

    // ✅ ALWAYS returns a NEW, VALID connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
