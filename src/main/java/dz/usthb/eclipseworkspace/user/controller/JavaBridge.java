package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.common.json.GsonProvider;
import dz.usthb.eclipseworkspace.task.controller.TaskController;
import dz.usthb.eclipseworkspace.team.controller.TeamController;
import dz.usthb.eclipseworkspace.todo.controller.TodoController;
import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.util.UserRole;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java ↔ JS Bridge
 * SINGLE SOURCE OF TRUTH between JavaFX and JS
 */
public class JavaBridge {

    private final MainController mainController;
    private final AuthService authService;
    private final UserService userService;
    private final WorkspaceController workspaceController;
    private final TodoController todoController;
    private final TaskController taskController;
    private final TeamController teamController;

    private final TeamMemberDaoJdbc teamMemberDao = new TeamMemberDaoJdbc();

    public JavaBridge(
            MainController mainController,
            AuthService authService,
            UserService userService,
            WorkspaceController workspaceController,
            TodoController todoController,
            TaskController taskController,
            TeamController teamController
    ) {
        this.mainController = mainController;
        this.authService = authService;
        this.userService = userService;
        this.workspaceController = workspaceController;
        this.todoController = todoController;
        this.taskController = taskController;
        this.teamController = teamController;

        System.out.println("✓ JavaBridge initialized");
    }
    // =====================================================
    // REGISTER
    // =====================================================
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


    // =====================================================
    // AUTH
    // =====================================================

    public String login(String email, String password) {
        try {
            String token = authService.login(email.trim().toLowerCase(), password);
            if (token == null) return "{\"success\":false}";
            mainController.loadPage("projects.html");
            return "{\"success\":true}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false}";
        }
    }

    public String logout() {
        Session.getInstance().clear();
        mainController.loadPage("register.html");
        return "{\"success\":true}";
    }

    
    // =====================================================
    // PROJECTS
    // =====================================================

    public String createProject(String name, String description, String invitedEmailsCsv) {

        Session session = Session.getInstance();
        if (!session.isAuthenticated()) {
            return "{\"success\":false,\"error\":\"Not authenticated\"}";
        }

        List<String> emails =
                invitedEmailsCsv == null || invitedEmailsCsv.isBlank()
                        ? List.of()
                        : Arrays.stream(invitedEmailsCsv.split(","))
                                .map(String::trim)
                                .filter(e -> !e.isEmpty())
                                .collect(Collectors.toList());

        int teamId = workspaceController.createProject(
                name,
                description,
                session.getUserId(),
                emails
        );

        return teamId > 0
                ? "{\"success\":true,\"teamId\":" + teamId + "}"
                : "{\"success\":false}";
    }
// =====================================================
// TASKS – NAVIGATION
// =====================================================

public void openNewTaskForm(int teamId) {
    mainController.openNewTaskForm(teamId);
}

    public String loadTeamMembers(int teamId) {
        try {
            var members = teamController.getTeamMembersView((long) teamId);
            return GsonProvider.get().toJson(members);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    // =====================================================
    // TASKS
    // =====================================================

    public String loadTasksAsJson(int teamId) {
        return taskController.loadTasksAsJson(teamId);
    }

    public void createTask(String title, String description, int teamId, String dueDate, Integer assigneeId) {

    System.out.println("🟥 createTask received teamId=" + teamId);

    if (teamId <= 0) {
        throw new IllegalArgumentException("INVALID teamId: " + teamId);
    }

    taskController.createTask(title, description, teamId, dueDate, assigneeId);
    mainController.goToWorkspace();
}


    public void updateTaskTitle(int taskId, String title) {
        taskController.updateTitle(taskId, title);
    }

    public void updateTaskDescription(int taskId, String description) {
        taskController.updateDescription(taskId, description);
    }

    public void updateTaskDueDate(int taskId, String dueDate) {
        taskController.updateDueDate(taskId, dueDate);
    }

    public void changeTaskStatus(int taskId, String action) {
        taskController.changeStatus(taskId, action);
        mainController.goToWorkspace();
    }

    public void deleteTaskById(int taskId) {
    taskController.deleteTaskById(taskId);
    mainController.reloadCurrentPage();
    }


    // =====================================================
    // NAVIGATION
    // =====================================================

    public void openTasksForProject(int teamId) {
        mainController.openTasksForProject(teamId);
    }

    public void openEditTask(int teamId, int taskId) {
        mainController.openEditTask(teamId, taskId);
    }

    
    public void deleteWorkspace(int teamId) {
        mainController.deleteWorkspace(teamId);
    }

    // =====================================================
    // PROFILE — SAFE (NO GSON)
    // =====================================================

    public String getCurrentUserInfo() {

        Session session = Session.getInstance();
        if (!session.isAuthenticated()) return "{}";

        User user = session.getCurrentUser();
        UserRole role = session.getRole();

        return String.format(
                "{\"userId\":%d,\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\"}",
                user.getUserId(),
                escape(user.getEmail()),
                escape(user.getFirstName()),
                escape(user.getLastName()),
                role.name()
        );
    }

    public boolean isAuthenticated() {
        return Session.getInstance().isAuthenticated();
    }

    public boolean isLead() {
        return Session.getInstance().isLead();
    }

    // =====================================================
    // UTILITY
    // =====================================================

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    // ===== TODO1 =====
    public void addTodo(String title, String description, String dueDate, String status) {
    try {
        todoController.addTodo(title, description, dueDate, status);
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    public void deleteTodo(int itemId) {
        try {
            todoController.deleteTodo(itemId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getUsername() {
    Session session = Session.getInstance();
    if (!session.isAuthenticated()) return "";

    return session.getCurrentUser().getFirstName()
           + " "
           + session.getCurrentUser().getLastName();
    }

    // ===== TODO2 =====
    public String loadMyTodos() {
        try {
            Long userId = Session.getInstance().getUserId();
            return todoController.loadTodosJson(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"progress\":0,\"tasks\":[]}";
        }
    }

    public void changeTodoStatus(int taskId, String status) {
        try {
            todoController.changeStatus(taskId, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
