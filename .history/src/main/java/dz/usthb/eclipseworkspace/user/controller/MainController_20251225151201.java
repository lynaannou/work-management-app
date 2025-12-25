package dz.usthb.eclipseworkspace.user.controller;

import com.google.gson.Gson;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
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

    private JavaBridge javaBridge;

    // 🔒 REMEMBERS LAST OPENED WORKSPACE
    private Integer lastWorkspaceId = null;

    public MainController(
            WebView webView,
            AuthService authService,
            UserService userService,
            WorkspaceService workspaceService,
            WorkspaceController workspaceController
    ) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.authService = authService;
        this.userService = userService;
        this.workspaceService = workspaceService;
        this.workspaceController = workspaceController;
        initialize();
    }

    /* ===============================
       INITIALIZATION
    =============================== */
    private void initialize() {

        webEngine.setOnAlert(e ->
                System.out.println("JS Alert: " + e.getData())
        );

        loadInitialPage();

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebEngine state: " + oldState + " -> " + newState);

            if (newState == Worker.State.SUCCEEDED) {
                exposeBridge();

                if (webEngine.getLocation().endsWith("projects.html")) {
                    loadProjects();
                }
            }
        });
    }

    private void loadInitialPage() {
        if (Session.getInstance().isAuthenticated()) {
            loadPage("projects.html");
        } else {
            loadPage("register.html");
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
                    workspaceController
            );
        }

        window.setMember("javaBridge", javaBridge);
        window.setMember("java", this);

        System.out.println("✓ Java bridge exposed");
    }

    /* ===============================
       PROJECT LIST / DASHBOARDS
    =============================== */
    private void loadProjects() {
        try {
            Long userId = Session.getInstance().getUserId();
            System.out.println("📦 Loading dashboards for user " + userId);

            List<WorkspaceDashboard> dashboards =
                    workspaceService.getDashboardsForUser(userId.intValue());

            String json = new Gson().toJson(dashboards);

            webEngine.executeScript(
                    "window.currentUserId=" + userId + "; loadProjects(" + json + ")"
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

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                try {
                    WorkspaceDashboard dashboard =
                            workspaceController.loadDashboard(teamId);

                    String json = new Gson().toJson(dashboard);
                    webEngine.executeScript("loadDashboard(" + json + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
       GENERIC NAVIGATION
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
}
