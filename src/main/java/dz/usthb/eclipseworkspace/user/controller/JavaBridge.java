package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.exception.AuthorizationException;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.UserRole;

/**
 * Complete JavaBridge with Authentication and Profile Management
 */
public class JavaBridge {

    private final MainController mainController;
    private final AuthService authService;
    private final UserService userService;

    public JavaBridge(MainController mainController, AuthService authService, UserService userService) {
        this.mainController = mainController;
        this.authService = authService;
        this.userService = userService;
        System.out.println("JavaBridge initialized with UserService");
    }

    // ============================================
    // AUTHENTICATION MODULE
    // ============================================

    /**
     * Login user
     */
    public String login(String email, String password) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return "ERROR: Email is required";
            }

            if (password == null || password.trim().isEmpty()) {
                return "ERROR: Password is required";
            }

            String token = authService.login(email.trim().toLowerCase(), password);

            if (token == null) {
                return "ERROR: Invalid email or password";
            }

            return "SUCCESS";

        } catch (AuthenticationException e) {
            // 🔐 Security: do NOT say which one is wrong
            return "ERROR: Invalid email or password";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Server error. Please try again.";
        }
    }


    /**
     * Register new user
     */
    public String register(String email, String firstName, String lastName, String password) {
        System.out.println("=== REGISTER METHOD CALLED ===");
        System.out.println("Email: " + email);

        try {
            if (email == null || email.trim().isEmpty()) {
                return "ERROR: Email is required";
            }
            if (firstName == null || firstName.trim().isEmpty()) {
                return "ERROR: First name is required";
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                return "ERROR: Last name is required";
            }
            if (password == null || password.trim().isEmpty()) {
                return "ERROR: Password is required";
            }

            User user = new User();
            user.setEmail(email.trim().toLowerCase());
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());

            authService.register(user, password.trim());

            System.out.println("✓ REGISTRATION SUCCESSFUL");
            return "SUCCESS";

        } catch (Exception e) {
            System.err.println("✗ Registration failed: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + (e.getMessage() != null ? e.getMessage() : "Registration failed");
        }
    }

    /**
     * Logout user
     */
    public String logout() {
        System.out.println("=== JAVA LOGOUT CALLED ===");

        try {
            Session session = Session.getInstance();

            if (session.getToken() != null) {
                authService.logout(session.getToken());
            }

            session.clear();
            System.out.println("Session cleared");

            mainController.loadPage("register.html");
            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }





    // ============================================
    // PROFILE MANAGEMENT MODULE
    // ============================================

    /**
     * Get current user's profile information
     * Returns JSON string with user data
     */
    public String getProfile() {
        System.out.println("=== GET PROFILE METHOD CALLED ===");

        try {
            // Get current user profile
            User user = userService.getCurrentUserProfile();

            // Return as JSON
            return String.format(
                    "{\"success\":true,\"user\":{" +
                            "\"userId\":%d," +
                            "\"email\":\"%s\"," +
                            "\"firstName\":\"%s\"," +
                            "\"lastName\":\"%s\"" +
                            "}}",
                    user.getUserId(),
                    user.getEmail(),
                    escapeJson(user.getFirstName()),
                    escapeJson(user.getLastName())
            );


        } catch (AuthenticationException e) {
            System.err.println("✗ Not authenticated: " + e.getMessage());
            mainController.loadPage("register.html");
            return "{\"success\":false,\"error\":\"Not authenticated\"}";

        } catch (Exception e) {
            System.err.println("✗ Error getting profile: " + e.getMessage());
            e.printStackTrace();
            return "{\"success\":false,\"error\":\"Failed to load profile\"}";
        }
    }

    /**
     * Update current user's profile (first name and last name)
     * Email cannot be changed
     */
    public String updateProfile(String firstName, String lastName) {
        System.out.println("=== UPDATE PROFILE METHOD CALLED ===");
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);

        try {
            // Validate input
            if (firstName == null || firstName.trim().isEmpty()) {
                return "ERROR: First name is required";
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                return "ERROR: Last name is required";
            }

            // Update profile
            User updatedUser = userService.updateCurrentUserProfile(
                    firstName.trim(),
                    lastName.trim()
            );

            System.out.println("✓ Profile updated successfully");

            // Return updated user data as JSON
            return "{\"success\":true}";


        } catch (AuthenticationException e) {
            System.err.println("✗ Not authenticated: " + e.getMessage());
            return "ERROR: Please log in to update profile";

        } catch (IllegalArgumentException e) {
            System.err.println("✗ Validation error: " + e.getMessage());
            return "ERROR: " + e.getMessage();

        } catch (Exception e) {
            System.err.println("✗ Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Failed to update profile";
        }
    }

    /**
     * Change current user's password
     */
    public String changePassword(String currentPassword, String newPassword, String confirmPassword) {
        System.out.println("=== CHANGE PASSWORD METHOD CALLED ===");

        try {
            // Validate input
            if (currentPassword == null || currentPassword.isEmpty()) {
                return "ERROR: Current password is required";
            }

            if (newPassword == null || newPassword.isEmpty()) {
                return "ERROR: New password is required";
            }

            if (newPassword.length() < 8) {
                return "ERROR: Password must be at least 8 characters";
            }

            if (!newPassword.equals(confirmPassword)) {
                return "ERROR: Passwords do not match";
            }

            // Change password
            userService.changePassword(currentPassword, newPassword);

            System.out.println("✓ Password changed successfully");
            return "SUCCESS";

        } catch (AuthenticationException e) {
            System.err.println("✗ Authentication error: " + e.getMessage());

            // Check if it's wrong current password
            if (e.getMessage().contains("incorrect") || e.getMessage().contains("Current password")) {
                return "ERROR: Current password is incorrect";
            }

            return "ERROR: Please log in to change password";

        } catch (IllegalArgumentException e) {
            System.err.println("✗ Validation error: " + e.getMessage());
            return "ERROR: " + e.getMessage();

        } catch (Exception e) {
            System.err.println("✗ Error changing password: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Failed to change password";
        }
    }


    // ============================================
    // NAVIGATION MODULE
    // ============================================

    /**
     * Navigate to a page (with authentication check)
     */
    public String navigateTo(String pageName) {
        System.out.println("=== NAVIGATE TO: " + pageName + " ===");

        // Public pages (no auth required)
        if (pageName.equals("register.html") || pageName.equals("login.html")) {
            mainController.loadPage(pageName);
            return "SUCCESS";
        }

        // Protected pages (auth required)
        Session session = Session.getInstance();
        if (!session.isAuthenticated()) {
            System.out.println("Not authenticated, redirecting to login");
            mainController.loadPage("register.html");
            return "ERROR: Not authenticated";
        }

        mainController.loadPage(pageName);
        return "SUCCESS";
    }

    /**
     * Get current user info (for displaying in UI)
     */
    public String getCurrentUserInfo() {
        Session session = Session.getInstance();
        if (!session.isAuthenticated()) {
            return "{}";
        }

        User user = session.getCurrentUser();
        UserRole role = session.getRole();

        return String.format(
                "{\"userId\":%d,\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\"}",
                user.getUserId(),
                user.getEmail(),
                escapeJson(user.getFirstName()),
                escapeJson(user.getLastName()),
                role.name()
        );
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return Session.getInstance().isAuthenticated();
    }

    /**
     * Check if user is a team lead
     */
    public boolean isLead() {
        return Session.getInstance().isLead();
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}