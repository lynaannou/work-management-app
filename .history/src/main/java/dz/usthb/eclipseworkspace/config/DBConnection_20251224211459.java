package dz.usthb.eclipseworkspace.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;

    private static final String URL =
            "jdbc:postgresql://localhost:5432/eclipseworkspace";
    private static final String USER = "postgres";
    private static final String PASSWORD = "amira";

    public static Connection getConnection() throws SQLException {

        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try {
            System.out.println("🔧 Loading PostgreSQL driver...");
            Class.forName("org.postgresql.Driver");

            System.out.println("🔌 Connecting to DB:");
            System.out.println("   URL  = " + URL);
            System.out.println("   USER = " + USER);

            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✔ Connected to PostgreSQL.");

            return connection;

        } catch (ClassNotFoundException e) {
            throw new SQLException("❌ PostgreSQL JDBC Driver NOT FOUND", e);

        } catch (SQLException e) {
            System.err.println("❌ JDBC CONNECTION ERROR");
            throw e;
        }
    }
}
