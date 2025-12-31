package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.task.controller.TaskController;
import dz.usthb.eclipseworkspace.todo.controller.TodoController;
import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.UserRole;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;

/**
 * Java ↔ JS Bridge
 * Authentication, profile, todos, tasks, navigation
 */
public class JavaBridge {

    private final MainController mainController;
    private final AuthService authService;
    private final UserService userService;
    private final dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController workspaceController;
    private final TodoController todoController;
    private final TaskController taskController;

    public JavaBridge(
            MainController mainController,
            AuthService authService,
            UserService userService,
            dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController workspaceController, // kept
            TodoController todoController,
            TaskController taskController
    ) {
        this.mainController = mainController;
        this.authService = authService;
        this.userService = userService;
        this.workspaceController = workspaceController;
        this.todoController = todoController;
        this.taskController = taskController;

        System.out.println("JavaBridge initialized with UserService + WorkspaceController");
    }

    // ============================================
    // DEBUG PING
    // ============================================

    public void debugPing(int teamId) {
        System.out.println("✅ [JavaBridge] debugPing CALLED teamId=" + teamId);
    }
    // ============================================
    // AUTHENTICATION
    // ============================================

    public String login(String email, String password) {
        try {
            if (email == null || email.isBlank()) return "ERROR: Email is required";
            if (password == null || password.isBlank()) return "ERROR: Password is required";

            String token = authService.login(email.trim().toLowerCase(), password);
            if (token == null) return "ERROR: Invalid email or password";

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

    // ============================================
    // TASKS – NAVIGATION
    // ============================================

    public void openTasksForProject(int teamId) {
        System.out.println("🟦 [JavaBridge] openTasksForProject teamId=" + teamId);
        mainController.openTasksForProject(teamId);
    }

    public void openTasks(int teamId) {
        mainController.openTasksForProject(teamId);
    }

    public void openEditTask(int teamId, int taskId) {
        System.out.println("🟦 [JavaBridge] openEditTask teamId=" + teamId + ", taskId=" + taskId);
        mainController.openEditTask(teamId, taskId);
    }

    // ============================================
    // TASKS – LOAD
    // ============================================

    public String loadTasksAsJson(int teamId) {
        System.out.println("🟨 [JavaBridge] loadTasksAsJson teamId=" + teamId);
        try {
            String json = taskController.loadTasksAsJson(teamId);
            System.out.println("🟦 [JavaBridge] JSON length=" + json.length());
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    // ============================================
    // TASKS – CREATE
    // ============================================

    public void createTask(
            String title,
            String description,
            int teamId,
            String dueDate,
            Integer assignedMemberId
    ) {
        System.out.println("🟦 [JavaBridge] createTask()");
        taskController.createTask(title, description, teamId, dueDate, assignedMemberId);
        mainController.goToWorkspace();
    }

    // ============================================
    // TASKS – PARTIAL UPDATES (🔥 IMPORTANT)
    // ============================================

    public void updateTaskTitle(int taskId, String title) {
        System.out.println("🟦 [JavaBridge] updateTaskTitle taskId=" + taskId);
        taskController.updateTitle(taskId, title);
    }

    public void updateTaskDescription(int taskId, String description) {
        System.out.println("🟦 [JavaBridge] updateTaskDescription taskId=" + taskId);
        taskController.updateDescription(taskId, description);
    }

    public void updateTaskDueDate(int taskId, String dueDateIso) {
        System.out.println("🟦 [JavaBridge] updateTaskDueDate taskId=" + taskId);
        taskController.updateDueDate(taskId, dueDateIso);
    }

    public void changeTaskStatus(int taskId, String action) {
        System.out.println("🟦 [JavaBridge] changeTaskStatus taskId=" + taskId + ", action=" + action);
        taskController.changeStatus(taskId, action);
        mainController.goToWorkspace();
    }

    public void deleteTask(int taskId) {
        System.out.println("🟦 [JavaBridge] deleteTask taskId=" + taskId);
        taskController.deleteTask(taskId);
        mainController.goToWorkspace();
    }

    // ============================================
    // TODOS
    // ============================================

    public String loadMyTodos() {
        try {
            Session session = Session.getInstance();
            if (!session.isAuthenticated()) {
                return "{\"error\":\"Not authenticated\"}";
            }
            Long userId = session.getUserId();
            return todoController.loadTodosJson(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Failed to load todos\"}";
        }
    }

    public void changeTodoStatus(int taskId, String status) {
        try {
            todoController.changeStatus(taskId, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================
    // PROFILE
    // ============================================

    public String getProfile() {
        try {
            User user = userService.getCurrentUserProfile();
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
            mainController.loadPage("register.html");
            return "{\"success\":false,\"error\":\"Not authenticated\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false,\"error\":\"Profile error\"}";
        }
    }

    public String updateProfile(String firstName, String lastName) {
        try {
            if (firstName == null || firstName.isBlank()) return "ERROR: First name required";
            if (lastName == null || lastName.isBlank()) return "ERROR: Last name required";

            userService.updateCurrentUserProfile(firstName.trim(), lastName.trim());
            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Update failed";
        }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    public String navigateTo(String pageName) {
        Session session = Session.getInstance();

        if (!session.isAuthenticated()
                && !pageName.equals("register.html")
                && !pageName.equals("login.html")) {
            mainController.loadPage("register.html");
            return "ERROR";
        }

        mainController.loadPage(pageName);
        return "SUCCESS";
    }

    public String getCurrentUserInfo() {
        Session session = Session.getInstance();
        if (!session.isAuthenticated()) return "{}";

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
    // ============================================
// WORKSPACE – DELETE
// ============================================
public void deleteWorkspace(int teamId) {
    try {
        mainController.deleteWorkspace(teamId);
    } catch (AuthenticationException e) {
        System.err.println("❌ Not authenticated");
    } catch (SecurityException e) {
        System.err.println("❌ Not authorized");
    } catch (Exception e) {
        e.printStackTrace();
    }
}



}
