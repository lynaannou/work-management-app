package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.common.json.GsonProvider;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.task.controller.TaskController;
import dz.usthb.eclipseworkspace.todo.controller.TodoController;
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

    private JavaBridge javaBridge;

    // 🔒 REMEMBERS LAST OPENED WORKSPACE
    private Integer lastWorkspaceId = null;

    // 🔒 used ONLY for edit-task navigation
    private Integer pendingTaskId = null;

    public MainController(
            WebView webView,
            AuthService authService,
            UserService userService,
            WorkspaceService workspaceService,
            WorkspaceController workspaceController,
            TodoController todoController,
            TaskController taskController
    ) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.authService = authService;
        this.userService = userService;
        this.workspaceService = workspaceService;
        this.workspaceController = workspaceController;
        this.todoController = todoController;
        this.taskController = taskController;
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
    // ===============================
// WORKSPACE DASHBOARD INJECTION
// ===============================
private void injectWorkspaceDashboard() {

    if (lastWorkspaceId == null) {
        System.err.println("❌ No workspace selected, cannot load dashboard");
        return;
    }

    try {
        System.out.println("📤 Injecting dashboard for workspaceId=" + lastWorkspaceId);

        WorkspaceDashboard dashboard =
                workspaceService.getDashboard(lastWorkspaceId);

        String json = GsonProvider.get().toJson(dashboard);

        webEngine.executeScript(
                // 🔑 REQUIRED BY DELETE BUTTON
                "window.currentTeamId = " + lastWorkspaceId + ";" +

                "if (typeof window.loadDashboard === 'function') {" +
                "   window.loadDashboard(" + json + ");" +
                "} else {" +
                "   console.error('loadDashboard is not defined');" +
                "}"
        );

    } catch (Exception e) {
        e.printStackTrace();
    }
}



    /* ===============================
       JAVA ↔ JS BRIDGE
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
                    taskController
            );
        }

        window.setMember("javaBridge", javaBridge);
        window.setMember("java", this);

        System.out.println("✓ Java bridge exposed");
    }

    /* ===============================
       PAGE LOAD DISPATCHER (FIXED)
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
        injectWorkspaceDashboard();   // ✅ MISSING CALL
        return;
    }

        if (location.endsWith("task-form.html")) {
            injectTaskForEdit();
        }
    }

    /* ===============================
       PROJECT LIST
    =============================== */
    private void loadProjects() {
        try {
            Long userId = Session.getInstance().getUserId();
            System.out.println("📦 Loading dashboards for user " + userId);

            List<WorkspaceDashboard> dashboards =
                    workspaceService.getDashboardsForUser(userId.intValue());

            String json = GsonProvider.get().toJson(dashboards);

            webEngine.executeScript(
                    "window.currentUserId=" + userId + ";" +
                    "if (typeof loadProjects === 'function') { loadProjects(" + json + "); }"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===============================
       WORKSPACE NAVIGATION
    =============================== */

    // 🔹 Open a workspace AND remember it
    public void openWorkspace(int teamId) {
        this.lastWorkspaceId = teamId;
        System.out.println("📌 Stored lastWorkspaceId = " + teamId);
        loadPage("workspace.html");
    }

    // 🔹 Go BACK to the workspace we came from
    public void goToWorkspace() {

        if (lastWorkspaceId == null) {
            System.err.println("⚠ No workspace to return to — going to projects");
            goProjects();
            return;
        }

        System.out.println("↩ Returning to workspace " + lastWorkspaceId);
        openWorkspace(lastWorkspaceId);
    }

    /* ===============================
       EDIT PROJECT
    =============================== */

    // 🔹 From workspace → edit-project.html
    public void editProject() {

        if (lastWorkspaceId == null) {
            System.err.println("⚠ editProject called with no workspace context");
            return;
        }

        System.out.println("✏ Opening edit-project for workspace " + lastWorkspaceId);
        loadPage("edit-project.html");
    }

    /* ===============================
       TODOS
    =============================== */
    private void loadTodos() {
        try {
            Long userId = Session.getInstance().getUserId();
            String todosJson = todoController.loadTodosJson(userId);

            webEngine.executeScript(
                    "if (typeof loadTodoList === 'function') { loadTodoList(" + todosJson + "); }"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goTasks() {
        loadPage("index.html");
    }

    /* ===============================
       NAVIGATION
    =============================== */
    public void loadPage(String page) {
        Platform.runLater(() -> {
            String url = getClass()
                    .getResource("/ressources/view/" + page)
                    .toExternalForm();

            System.out.println("Loading page: " + url);
            webEngine.load(url);
        });
    }

    public void openNewProject() {
        System.out.println("➕ Opening NEW PROJECT page");
        loadPage("project-form.html");
    }

    public void goProjects() {
        System.out.println("⬅ Navigating to PROJECTS");
        loadPage("projects.html");
    }

    public void logout() {
        Session.getInstance().clear();
        loadPage("register.html");
    }

    /* ===============================
       TASKS PER PROJECT
    =============================== */
    public void openTasksForProject(int teamId) {
        this.lastWorkspaceId = teamId;
        System.out.println("📋 Opening tasks for teamId = " + teamId);
        loadPage("track_tasks.html");
    }

    private void injectTeamForTasks() {
        if (lastWorkspaceId == null) return;

        System.out.println("📤 Injecting currentTeamId into JS = " + lastWorkspaceId);

        webEngine.executeScript(
                "window.currentTeamId = " + lastWorkspaceId + ";" +
                "if (typeof loadTasks === 'function') { loadTasks(); }"
        );
    }

    /* ===============================
       EDIT TASK (FIXED & SAFE)
    =============================== */
    public void openEditTask(int teamId, int taskId) {

        this.lastWorkspaceId = teamId;
        this.pendingTaskId = taskId;

        System.out.println("✏️ Opening edit task page for taskId = " + taskId);

        loadPage("task-form.html");
    }

    private void injectTaskForEdit() {
        if (lastWorkspaceId == null || pendingTaskId == null) return;

        System.out.println(
                "📤 Injecting teamId=" + lastWorkspaceId +
                " taskId=" + pendingTaskId
        );

        webEngine.executeScript(
                "window.currentTeamId = " + lastWorkspaceId + ";" +
                "window.currentTaskId = " + pendingTaskId + ";" +
                "if (typeof loadTaskForEdit === 'function') { loadTaskForEdit(); }"
        );

        pendingTaskId = null; // ✅ prevent double injection
    }
public void deleteWorkspace(int teamId) {
    System.out.println("🟢 MainController.deleteWorkspace teamId=" + teamId);

    try {
        workspaceController.deleteWorkspace(teamId);
        lastWorkspaceId = null;

        System.out.println("🟢 Workspace deleted, returning to projects");

        goProjects();
    } catch (Exception e) {
        System.err.println("🔴 MainController.deleteWorkspace FAILED");
        e.printStackTrace();
    }
}


}
