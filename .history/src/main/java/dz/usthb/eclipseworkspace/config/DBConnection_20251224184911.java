package dz.usthb.eclipseworkspace.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;

    // Load .env variables
    private static final String URL = "jdbc:postgresql://localhost:5432/eclipseworkspace";
    private static final String USER = "postgres";
    private static final String PASSWORD = "amira";
    // Returns ONE connection that you reuse everywhere
    public static Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {
            try {
                // Load PostgreSQL driver (optional with modern JDBC, but safe)
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✔ Connected to PostgreSQL.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("❌ PostgreSQL driver not found.", e);
            }
        }

        return connection;
    }
}
