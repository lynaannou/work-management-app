package dz.usthb.eclipseworkspace.user.dao;

import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.user.model.User;

import java.sql.*;
import java.util.Optional;

/**
 * FIXED UserDao - Properly sets userId after INSERT
 */
public class UserDao {

    /**
     * Create a new user and SET the userId on the user object
     * ✅ CRITICAL: This method MUST set user.setUserId() after insertion
     */
    public void create(User user) throws SQLException {
        String sql = "INSERT INTO app_user (email, username, first_name, last_name, phone, password_hash, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Extract username from email (part before @)
            String username = user.getEmail().split("@")[0];

            ps.setString(1, user.getEmail());
            ps.setString(2, username);
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getPasswordHash());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // ✅ CRITICAL FIX: Get the generated user_id and set it on the user object
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long userId = generatedKeys.getLong(1);
                    user.setUserId(userId);
                    System.out.println("✅ User created with ID: " + userId);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, email, username, first_name, last_name, phone, password_hash, created_at " +
                "FROM app_user WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long userId) throws SQLException {
        String sql = "SELECT user_id, email, username, first_name, last_name, phone, password_hash, created_at " +
                "FROM app_user WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM app_user WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Update user profile (first name, last name)
     */
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE app_user SET first_name = ?, last_name = ?, phone = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getPhone());
            ps.setLong(4, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Update user password
     */
    public boolean updatePassword(Long userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE app_user SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Map ResultSet to User object
     * ✅ IMPORTANT: Always map user_id from the database
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        // ✅ CRITICAL: Set the userId from database
        user.setUserId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));

        return user;
    }
}