package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.UserRole;
import javafx.scene.web.WebEngine;
import java.io.StringWriter;
import java.io.PrintWriter;

public class JavaAuthBridge {

    private final AuthService authService;
    private final WebEngine webEngine;
    private final TeamMemberDao teamMemberDao;

    public JavaAuthBridge(AuthService authService, WebEngine webEngine) {
        this.authService = authService;
        this.webEngine = webEngine;
        this.teamMemberDao = new TeamMemberDaoJdbc();
        System.out.println("JavaAuthBridge initialized");
    }

    /**
     * Test method to verify bridge is working
     */
    public String test() {
        System.out.println("=== TEST METHOD CALLED ===");
        return "Bridge is working!";
    }

    /**
     * Login method - called from JavaScript
     */
    public String login(String email, String password) {
        System.out.println("=================================");
        System.out.println("=== LOGIN METHOD CALLED ===");
        System.out.println("Email parameter: " + email);
        System.out.println("Password parameter: " + (password != null ? "[PROVIDED]" : "[NULL]"));
        System.out.println("=================================");

        // Validate inputs first
        if (email == null || email.trim().isEmpty()) {
            System.err.println("ERROR: Email is null or empty");
            return "ERROR: Email is required";
        }

        if (password == null || password.trim().isEmpty()) {
            System.err.println("ERROR: Password is null or empty");
            return "ERROR: Password is required";
        }

        String token = null;
        User user = null;

        try {
            email = email.trim().toLowerCase();
            System.out.println("Processing login for: " + email);

            // Call auth service
            System.out.println("Calling authService.login()...");
            System.out.flush(); // Force output

            try {
                token = authService.login(email, password);
                System.out.println("✓ authService.login() completed");
                System.out.println("Token received: " + (token != null ? "YES" : "NULL"));
            } catch (Throwable t) {
                System.err.println("✗✗✗ EXCEPTION IN authService.login() ✗✗✗");
                System.err.println("Exception class: " + t.getClass().getName());
                System.err.println("Exception message: " + t.getMessage());
                System.err.println("Full stack trace:");
                t.printStackTrace(System.err);
                System.err.flush();

                String msg = t.getMessage();
                if (msg == null || msg.trim().isEmpty()) {
                    msg = t.getClass().getSimpleName();
                }
                return "ERROR: " + msg;
            }

            if (token == null) {
                System.err.println("ERROR: Token is null after login");
                return "ERROR: Authentication failed - no token returned";
            }

            // Verify token
            System.out.println("Verifying token...");
            System.out.flush();

            try {
                user = authService.verifyToken(token);
                System.out.println("✓ authService.verifyToken() completed");
                System.out.println("User: " + (user != null ? user.getEmail() : "NULL"));
            } catch (Throwable t) {
                System.err.println("✗✗✗ EXCEPTION IN authService.verifyToken() ✗✗✗");
                System.err.println("Exception class: " + t.getClass().getName());
                System.err.println("Exception message: " + t.getMessage());
                System.err.println("Full stack trace:");
                t.printStackTrace(System.err);
                System.err.flush();

                String msg = t.getMessage();
                if (msg == null || msg.trim().isEmpty()) {
                    msg = t.getClass().getSimpleName();
                }
                return "ERROR: " + msg;
            }

            if (user == null) {
                System.err.println("ERROR: User is null after verification");
                return "ERROR: User verification failed";
            }

            System.out.println("User verified: " + user.getEmail());

            // Get role from DB
            System.out.println("Getting user role...");
            String dbRole = null;
            try {
                dbRole = teamMemberDao.getRoleByUserId(user.getUserId());
                System.out.println("User role from DB: " + dbRole);
            } catch (Exception e) {
                System.out.println("Note: Could not get role from DB (user may not be in a team yet)");
                System.out.println("Using default MEMBER role");
            }

            // Convert String to enum
            UserRole role = dbRole != null ? UserRole.valueOf(dbRole.toUpperCase()) : UserRole.MEMBER;
            System.out.println("Using role: " + role);

            // Save in session
            System.out.println("Saving session...");
            try {
                Session.getInstance().setUser(user, token, role);
                System.out.println("✓ Session saved");
            } catch (Throwable t) {
                System.err.println("✗✗✗ EXCEPTION IN Session.setUser() ✗✗✗");
                System.err.println("Exception class: " + t.getClass().getName());
                System.err.println("Exception message: " + t.getMessage());
                t.printStackTrace(System.err);
                System.err.flush();

                String msg = t.getMessage();
                if (msg == null || msg.trim().isEmpty()) {
                    msg = t.getClass().getSimpleName();
                }
                return "ERROR: " + msg;
            }

            System.out.println("=================================");
            System.out.println("✓✓✓ LOGIN SUCCESSFUL ✓✓✓");
            System.out.println("=================================");
            return "SUCCESS";

        } catch (Throwable t) {
            System.err.println("=================================");
            System.err.println("✗✗✗ UNEXPECTED EXCEPTION IN login() ✗✗✗");
            System.err.println("Exception class: " + t.getClass().getName());
            System.err.println("Exception message: " + t.getMessage());
            System.err.println("Full stack trace:");
            t.printStackTrace(System.err);
            System.err.println("=================================");
            System.err.flush();

            String msg = t.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = t.getClass().getSimpleName();
            }
            return "ERROR: " + msg;
        }
    }

    /**
     * Register method - called from JavaScript
     */
    public String register(String email, String firstName, String lastName, String password) {
        System.out.println("=================================");
        System.out.println("=== REGISTER METHOD CALLED ===");
        System.out.println("Email: " + email);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Password: " + (password != null ? "[PROVIDED]" : "[NULL]"));
        System.out.println("=================================");

        // Validate inputs
        if (email == null || email.trim().isEmpty()) {
            System.err.println("ERROR: Email is null or empty");
            return "ERROR: Email is required";
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            System.err.println("ERROR: First name is null or empty");
            return "ERROR: First name is required";
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            System.err.println("ERROR: Last name is null or empty");
            return "ERROR: Last name is required";
        }

        if (password == null || password.trim().isEmpty()) {
            System.err.println("ERROR: Password is null or empty");
            return "ERROR: Password is required";
        }

        try {
            System.out.println("Creating user object...");
            User user = new User();

            try {
                user.setEmail(email.trim().toLowerCase());
                user.setFirstName(firstName.trim());
                user.setLastName(lastName.trim());
                System.out.println("✓ User object created");
            } catch (Throwable t) {
                System.err.println("✗✗✗ EXCEPTION creating User object ✗✗✗");
                System.err.println("Exception class: " + t.getClass().getName());
                System.err.println("Exception message: " + t.getMessage());
                t.printStackTrace(System.err);
                System.err.flush();

                String msg = t.getMessage();
                if (msg == null || msg.trim().isEmpty()) {
                    msg = t.getClass().getSimpleName();
                }
                return "ERROR: " + msg;
            }

            System.out.println("Calling authService.register()...");
            System.out.flush();

            try {
                authService.register(user, password.trim());
                System.out.println("✓ authService.register() completed");
            } catch (Throwable t) {
                System.err.println("✗✗✗ EXCEPTION IN authService.register() ✗✗✗");
                System.err.println("Exception class: " + t.getClass().getName());
                System.err.println("Exception message: " + t.getMessage());
                System.err.println("Full stack trace:");
                t.printStackTrace(System.err);
                System.err.flush();

                String msg = t.getMessage();
                if (msg == null || msg.trim().isEmpty()) {
                    msg = t.getClass().getSimpleName();
                }
                return "ERROR: " + msg;
            }

            System.out.println("=================================");
            System.out.println("✓✓✓ REGISTRATION SUCCESSFUL ✓✓✓");
            System.out.println("=================================");
            return "SUCCESS";

        } catch (Throwable t) {
            System.err.println("=================================");
            System.err.println("✗✗✗ UNEXPECTED EXCEPTION IN register() ✗✗✗");
            System.err.println("Exception class: " + t.getClass().getName());
            System.err.println("Exception message: " + t.getMessage());
            System.err.println("Full stack trace:");
            t.printStackTrace(System.err);
            System.err.println("=================================");
            System.err.flush();

            String msg = t.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = t.getClass().getSimpleName();
            }
            return "ERROR: " + msg;
        }
    }

    /**
     * Logout method - called from JavaScript
     */
    public String logout() {
        System.out.println("=== LOGOUT METHOD CALLED ===");
        try {
            Session session = Session.getInstance();
            if (session.getToken() != null) {
                authService.logout(session.getToken());
            }
            session.clear();
            System.out.println("✓ Logout successful");
            return "SUCCESS";
        } catch (Exception e) {
            System.err.println("✗ Logout failed: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + (e.getMessage() != null ? e.getMessage() : "Logout failed");
        }
    }

    /**
     * Load a new page in the WebView
     */
    public String loadPage(String pagePath) {
        System.out.println("=== LOADPAGE METHOD CALLED ===");
        System.out.println("Page path: " + pagePath);

        if (pagePath == null || pagePath.trim().isEmpty()) {
            System.err.println("ERROR: Page path is null or empty");
            return "ERROR: Page path is required";
        }

        try {
            String url = getClass().getResource("/ressources/view/" + pagePath).toExternalForm();
            System.out.println("Loading URL: " + url);
            webEngine.load(url);
            System.out.println("✓ Page load initiated successfully");
            return "SUCCESS";
        } catch (Exception e) {
            System.err.println("✗ Failed to load page: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + (e.getMessage() != null ? e.getMessage() : "Failed to load page");
        }
    }
}