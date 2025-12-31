package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.common.json.GsonProvider;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.task.controller.TaskController;
import dz.usthb.eclipseworkspace.todo.controller.TodoController;
import dz.usthb.eclipseworkspace.team.controller.TeamController;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.List;

public class MainController {

    private final WebView webView;
    private final WebEngine webEngine;
    private final AuthService authService;
    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final WorkspaceController workspaceController;
    private final TodoController todoController;
    private final TaskController taskController;
    private final TeamController teamController;

    private JavaBridge javaBridge;

    private Integer lastWorkspaceId = null;
    private Integer pendingTaskId = null;

    public MainController(
            WebView webView,
            AuthService authService,
            UserService userService,
            WorkspaceService workspaceService,
            WorkspaceController workspaceController,
            TodoController todoController,
            TaskController taskController,
            TeamController teamController
    ) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.authService = authService;
        this.userService = userService;
        this.workspaceService = workspaceService;
        this.workspaceController = workspaceController;
        this.todoController = todoController;
        this.taskController = taskController;
        this.teamController = teamController;
        initialize();
    }

    /* ===============================
       INITIALIZATION
    =============================== */
    private void initialize() {

        webEngine.setOnAlert(e ->
                System.out.println("JS Alert: " + e.getData())
        );

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state: " + oldState + " -> " + newState);

            if (newState == Worker.State.SUCCEEDED) {
                exposeBridge();
                handlePageLoad();
            }
        });

        loadInitialPage();
    }

    private void loadInitialPage() {
        if (Session.getInstance().isAuthenticated()) {
            loadPage("projects.html");
        } else {
            loadPage("register.html");
        }
    }
    /* ===============================
   WORKSPACE – DELETE
=============================== */
public void deleteWorkspace(int teamId) {
    System.out.println("🗑 Deleting workspace teamId=" + teamId);

    try {
        workspaceController.deleteWorkspace(teamId);

        // reset context
        lastWorkspaceId = null;

        // go back to projects list
        goProjects();

    } catch (Exception e) {
        System.err.println("❌ Failed to delete workspace");
        e.printStackTrace();
    }
}
/* ===============================
   TASKS – CREATE
=============================== */
public void openNewTaskForm(int teamId) {
    System.out.println("➕ Opening NEW TASK form for teamId=" + teamId);

    // remember workspace
    lastWorkspaceId = teamId;

    // no pendingTaskId → create mode
    pendingTaskId = null;

    loadPage("task-form.html");
}



    /* ===============================
       BRIDGE
    =============================== */
    private void exposeBridge() {
        JSObject window = (JSObject) webEngine.executeScript("window");

        if (javaBridge == null) {
            javaBridge = new JavaBridge(
                    this,
                    authService,
                    userService,
                    workspaceController,
                    todoController,
                    taskController,
                    teamController
            );
        }

        window.setMember("javaBridge", javaBridge);
        window.setMember("java", this);

        System.out.println("✓ Java bridge exposed");
    }

    /* ===============================
       PAGE DISPATCHER
    =============================== */
    private void handlePageLoad() {

        String location = webEngine.getLocation();

        if (location.endsWith("projects.html")) {
            loadProjects();
            return;
        }

        if (location.endsWith("index.html")) {
            loadTodos();
            return;
        }

        if (location.endsWith("track_tasks.html")) {
            injectTeamForTasks();
            return;
        }

        if (location.endsWith("workspace.html")) {
            injectWorkspaceDashboard();
            return;
        }

        if (location.endsWith("task-form.html")) {
            injectTaskContext();
        }
    }
    private void injectTaskContext() {

    if (lastWorkspaceId == null) {
        System.err.println("❌ injectTaskContext: lastWorkspaceId is null");
        return;
    }

    System.out.println("🟦 injectTaskContext teamId=" + lastWorkspaceId
            + ", taskId=" + pendingTaskId);

    if (pendingTaskId != null) {
        webEngine.executeScript(
            "window.currentTeamId=" + lastWorkspaceId + ";" +
            "window.currentTaskId=" + pendingTaskId + ";" +
            "if (typeof loadTaskForEdit === 'function') loadTaskForEdit();"
        );
    } else {
        webEngine.executeScript(
            "window.currentTeamId=" + lastWorkspaceId + ";" +
            "if (typeof loadTeamMembers === 'function') loadTeamMembers();"
        );
    }
}


    /* ===============================
       PROJECTS
    =============================== */
    private void loadProjects() {
        try {
            Long userId = Session.getInstance().getUserId();
            List<WorkspaceDashboard> dashboards =
                    workspaceService.getDashboardsForUser(userId.intValue());

            String json = GsonProvider.get().toJson(dashboards);

            webEngine.executeScript(
                    "window.currentUserId=" + userId +
                    "; if (typeof loadProjects === 'function') loadProjects(" + json + ");"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       WORKSPACE
    =============================== */
    public void openWorkspace(int teamId) {
        lastWorkspaceId = teamId;
        loadPage("workspace.html");
    }

    private void injectWorkspaceDashboard() {
        if (lastWorkspaceId == null) return;

        try {
            WorkspaceDashboard dashboard =
                    workspaceService.getDashboard(lastWorkspaceId);

            String json = GsonProvider.get().toJson(dashboard);

            webEngine.executeScript(
                    "window.currentTeamId=" + lastWorkspaceId +
                    "; if (typeof loadDashboard === 'function') loadDashboard(" + json + ");"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToWorkspace() {
        if (lastWorkspaceId == null) {
            goProjects();
        } else {
            openWorkspace(lastWorkspaceId);
        }
    }

    /* ===============================
       TODOS
    =============================== */
    private void loadTodos() {
        try {
            Long userId = Session.getInstance().getUserId();
            String todosJson = todoController.loadTodosJson(userId);
            webEngine.executeScript("loadTodoList(" + todosJson + ");");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       TASKS
    =============================== */
    public void openTasksForProject(int teamId) {
        lastWorkspaceId = teamId;
        loadPage("track_tasks.html");
    }

    private void injectTeamForTasks() {
        if (lastWorkspaceId == null) return;

        webEngine.executeScript(
                "window.currentTeamId=" + lastWorkspaceId +
                "; if (typeof loadTasks === 'function') loadTasks();"
        );
    }

    public void openEditTask(int teamId, int taskId) {
        lastWorkspaceId = teamId;
        pendingTaskId = taskId;
        loadPage("task-form.html");
    }

    private void injectTaskForEdit() {
        if (lastWorkspaceId == null || pendingTaskId == null) return;

        webEngine.executeScript(
                "window.currentTeamId=" + lastWorkspaceId +
                "; window.currentTaskId=" + pendingTaskId +
                "; if (typeof loadTaskForEdit === 'function') loadTaskForEdit();"
        );

        pendingTaskId = null;
    }

    /* ===============================
       NAVIGATION
    =============================== */
    public void loadPage(String page) {
        Platform.runLater(() -> {
            String url = getClass()
                    .getResource("/ressources/view/" + page)
                    .toExternalForm();
            webEngine.load(url);
        });
    }

    public void openNewProject() {
        loadPage("project-form.html");
    }

    public void goProjects() {
        loadPage("projects.html");
    }

    public void goTasks() {
        loadPage("index.html");
    }

    public void logout() {
        Session.getInstance().clear();
        loadPage("register.html");
    }
}
