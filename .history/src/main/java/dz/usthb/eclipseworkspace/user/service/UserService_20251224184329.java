package dz.usthb.eclipseworkspace.user.service;

import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.PasswordHashStrategy;
import dz.usthb.eclipseworkspace.user.util.Session;

import java.sql.SQLException;

/**
 * User Service - Handles user profile operations with security
 *
 * This service manages user profile viewing and updates.
 * All methods require authentication.
 */
public class UserService {

    private final SecurityService security = SecurityService.getInstance();
    private final UserDao userDao;
    private final PasswordHashStrategy passwordHashStrategy;

    public UserService(UserDao userDao, PasswordHashStrategy passwordHashStrategy) {
        this.userDao = userDao;
        this.passwordHashStrategy = passwordHashStrategy;
    }

    // ======================================
    // PROFILE VIEWING
    // ======================================

    /**
     * Get current user's profile
     * User must be authenticated
     *
     * @return current user's profile
     * @throws AuthenticationException if not authenticated
     * @throws SQLException if database error occurs
     */
    public User getCurrentUserProfile() throws SQLException {
        //Security check - user must be authenticated
        security.requireAuthentication();

        Long userId = security.getCurrentUserId();

        return userDao.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found in database"));
    }

    /**
     * Get user profile by ID
     * Only accessible by the user themselves or LEADs
     *
     * @param userId the user ID to get profile for
     * @return user profile
     * @throws AuthenticationException if not authenticated
     * @throws SQLException if database error occurs
     */
    public User getUserProfile(Long userId) throws SQLException {
        //Security check - must be able to view this user
        security.requireCanViewUser(userId);

        return userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ======================================
    // PROFILE UPDATE
    // ======================================

    /**
     * Update current user's profile (first name and last name)
     * User must be authenticated
     * Email cannot be changed
     *
     * @param firstName new first name
     * @param lastName new last name
     * @return updated user
     * @throws AuthenticationException if not authenticated
     * @throws SQLException if database error occurs
     */
    public User updateCurrentUserProfile(String firstName, String lastName) throws SQLException {
        //Security check
        security.requireAuthentication();

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        Long userId = security.getCurrentUserId();

        // Get current user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Update profile fields
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());

        // Save to database
        boolean updated = userDao.updateProfile(user);

        if (!updated) {
            throw new SQLException("Failed to update profile");
        }

        // Update session with new information
        Session.getInstance().setUser(
                user,
                Session.getInstance().getToken(),
                Session.getInstance().getRole()
        );

        System.out.println("Profile updated for user: " + userId);

        return user;
    }

    /**
     * Update user profile by ID (admin function)
     * Only LEADs can update other users' profiles
     *
     * @param userId the user ID to update
     * @param firstName new first name
     * @param lastName new last name
     * @return updated user
     * @throws SQLException if database error occurs
     */
    public User updateUserProfile(Long userId, String firstName, String lastName) throws SQLException {
        // Security check - must be able to modify this user
        security.requireCanModifyUser(userId);

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        // Get user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update fields
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());

        // Save to database
        boolean updated = userDao.updateProfile(user);

        if (!updated) {
            throw new SQLException("Failed to update profile");
        }

        System.out.println("Profile updated for user: " + userId);

        return user;
    }

    // ======================================
    // PASSWORD MANAGEMENT
    // ======================================

    /**
     * Change current user's password
     * User must be authenticated and provide correct current password
     *
     * @param currentPassword the current password (plain text)
     * @param newPassword the new password (plain text)
     * @throws AuthenticationException if not authenticated or current password is wrong
     * @throws SQLException if database error occurs
     */
    public void changePassword(String currentPassword, String newPassword) throws SQLException {
        //Security check
        security.requireAuthentication();

        // Validate input
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }

        // Prevent reusing the same password
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        Long userId = security.getCurrentUserId();

        // Get current user
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Verify current password
        if (!passwordHashStrategy.verifyPassword(currentPassword, user.getPasswordHash())) {
            System.err.println("Invalid current password for user: " + userId);
            throw new AuthenticationException("Current password is incorrect");
        }

        // Hash new password
        String newPasswordHash = passwordHashStrategy.hashPassword(newPassword);

        // Update in database
        boolean updated = userDao.updatePassword(userId, newPasswordHash);

        if (!updated) {
            throw new SQLException("Failed to update password");
        }

        System.out.println(" Password changed for user: " + userId);
    }

    /**
     * Reset user password (admin function)
     * Only LEADs can reset passwords for other users
     *
     * @param userId the user ID
     * @param newPassword the new password (plain text)
     * @throws SQLException if database error occurs
     */
    public void resetUserPassword(Long userId, String newPassword) throws SQLException {
        // Security check - must be LEAD
        security.requireLead();

        // Validate input
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Verify user exists
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Hash new password
        String newPasswordHash = passwordHashStrategy.hashPassword(newPassword);

        // Update in database
        boolean updated = userDao.updatePassword(userId, newPasswordHash);

        if (!updated) {
            throw new SQLException("Failed to reset password");
        }

        System.out.println("Password reset for user: " + userId);
    }

    // ======================================
    // USER INFORMATION
    // ======================================

    /**
     * Get user's full name
     *
     * @param userId the user ID
     * @return full name (first + last)
     * @throws SQLException if database error occurs
     */
    public String getUserFullName(Long userId) throws SQLException {
        // Security check
        security.requireCanViewUser(userId);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getFirstName() + " " + user.getLastName();
    }

    /**
     * Check if user exists by ID
     *
     * @param userId the user ID
     * @return true if user exists
     * @throws SQLException if database error occurs
     */
    public boolean userExists(Long userId) throws SQLException {
        // Security check
        security.requireAuthentication();

        return userDao.findById(userId).isPresent();
    }

    /**
     * Check if email is available for registration
     * Public method - no authentication required
     *
     * @param email the email to check
     * @return true if email is available
     * @throws SQLException if database error occurs
     */
    public boolean isEmailAvailable(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return !userDao.emailExists(email.trim().toLowerCase());
    }
}