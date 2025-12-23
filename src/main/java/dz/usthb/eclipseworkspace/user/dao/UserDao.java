package dz.usthb.eclipseworkspace.user.dao;
import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.user.model.User;
import java.sql.*;
import java.util.Optional;

public class UserDao{

    public void create(User user) throws SQLException {
        String sql = "INSERT INTO app_user (email, username, first_name, last_name, phone, password_hash) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getPasswordHash());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getLong(1));
                }
            }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setUsername(rs.getString("username"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setUsername(rs.getString("username"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE app_user SET first_name=?, last_name=?, phone=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getPhone());
            stmt.setLong(4, user.getUserId());
            stmt.executeUpdate();
        }
    }
}

