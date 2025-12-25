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

import com.google.gson.Gson;

public class JavaBridge {

    private final MainController mainController;
    private final AuthService authService;
    private final UserService userService;
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
    // AUTHENTICATION
    // ============================================

    public String login(String email, String password) {
        try {
            if (email == null || email.isBlank()) {
                return "{\"success\":false,\"error\":\"Email is required\"}";
            }

            if (password == null || password.isBlank()) {
                return "{\"success\":false,\"error\":\"Password is required\"}";
            }

            String token = authService.login(email.trim().toLowerCase(), password);

            if (token == null) {
                return "{\"success\":false,\"error\":\"Invalid email or password\"}";
            }

            mainController.loadPage("projects.html");
            return "{\"success\":true}";

        } catch (AuthenticationException e) {
            return "{\"success\":false,\"error\":\"Invalid email or password\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false,\"error\":\"Server error\"}";
        }
    }

    // ============================================
    // ✅ REGISTRATION (FIXED & ADDED)
    // ============================================

    public String register(String email, String firstName, String lastName, String password) {
        try {
            if (email == null || email.isBlank()
                    || firstName == null || firstName.isBlank()
                    || lastName == null || lastName.isBlank()
                    || password == null || password.isBlank()) {

                return "{\"success\":false,\"error\":\"All fields are required\"}";
            }

            User user = userService.register(
                    email.trim().toLowerCase(),
                    firstName.trim(),
                    lastName.trim(),
                    password
            );

            if (user == null) {
                return "{\"success\":false,\"error\":\"Registration failed\"}";
            }

            return "{\"success\":true}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false,\"error\":\"Email already exists or server error\"}";
        }
    }

    // ============================================
    // PROJECT / WORKSPACE
    // ============================================

    public String createProject(String name, String description, String invitedEmailsCsv) {
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

            return teamId > 0
                    ? "{\"success\":true,\"teamId\":" + teamId + "}"
                    : "{\"success\":false,\"error\":\"Project creation failed\"}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false,\"error\":\"Server error\"}";
        }
    }

    // ============================================
    // SESSION / PROFILE
    // ============================================

    public String logout() {
        try {
            Session session = Session.getInstance();
            if (session.getToken() != null) {
                authService.logout(session.getToken());
            }
            session.clear();
            mainController.loadPage("register.html");
            return "{\"success\":true}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
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
    // USERS SEARCH (INVITE)
    // ============================================

    public String searchUsersByEmail(String prefix) {
        try {
            List<User> users = userService.searchUsersByEmail(prefix);
            return new Gson().toJson(users);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    // ============================================
    // UTILITY
    // ============================================

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
