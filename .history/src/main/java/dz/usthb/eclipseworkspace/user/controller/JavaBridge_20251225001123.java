package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.UserRole;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Complete JavaBridge with Authentication and Profile Management
 */
public class JavaBridge {

    private final MainController mainController;
    private final AuthService authService;
    private final UserService userService;

    // 🔽 ONLY ADDITION
    private final WorkspaceController workspaceController;

    public JavaBridge(
            MainController mainController,
            AuthService authService,
            UserService userService,
            WorkspaceController workspaceController
    ) {
        this.mainController = mainController;
        this.authService = authService;
        this.userService = userService;
        this.workspaceController = workspaceController;

        System.out.println("JavaBridge initialized with UserService + WorkspaceController");
    }

    // ============================================
    // AUTHENTICATION MODULE
    // ============================================

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
            mainController.loadPage("projects.html");
            return "SUCCESS";

        } catch (AuthenticationException e) {
            return "ERROR: Invalid email or password";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Server error. Please try again.";
        }
    }

    // ============================================
    // PROJECT / WORKSPACE CREATION
    // ============================================

    /**
     * Create a new project (workspace/team)
     * Called directly from JS
     */
    public String createProject(
            String name,
            String description,
            String invitedEmailsCsv
    ) {
        try {
            Session session = Session.getInstance();

            if (!session.isAuthenticated()) {
                return "{\"success\":false,\"error\":\"Not authenticated\"}";
            }

            long leaderUserId = session.getUserId();

            List<String> emails = invitedEmailsCsv == null || invitedEmailsCsv.isBlank()
                    ? List.of()
                    : Arrays.stream(invitedEmailsCsv.split(","))
                            .map(String::trim)
                            .filter(e -> !e.isEmpty())
                            .collect(Collectors.toList());

            int teamId = workspaceController.createProject(
                    name,
                    description,
                    leaderUserId,
                    emails
            );

            if (teamId <= 0) {
                return "{\"success\":false,\"error\":\"Project creation failed\"}";
            }

            return "{\"success\":true,\"teamId\":" + teamId + "}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false,\"error\":\"Server error\"}";
        }
    }

    // ============================================
    // PROFILE + NAVIGATION (UNCHANGED)
    // ============================================

    public String logout() {
        try {
            Session session = Session.getInstance();

            if (session.getToken() != null) {
                authService.logout(session.getToken());
            }

            session.clear();
            mainController.loadPage("register.html");
            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

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

    public boolean isAuthenticated() {
        return Session.getInstance().isAuthenticated();
    }

    public boolean isLead() {
        return Session.getInstance().isLead();
    }

    // ============================================
    // UTILITY
    // ============================================

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
