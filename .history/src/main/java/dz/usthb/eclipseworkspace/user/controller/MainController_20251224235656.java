package dz.usthb.eclipseworkspace.user.controller;

import com.google.gson.Gson;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import java.net.URL;

public class MainController {

    private final WebView webView;
    private final WebEngine webEngine;
    private final AuthService authService;
    private final UserService userService;
   
    private final WorkspaceController workspaceController;

    private JavaBridge javaBridge;

    public MainController(
            WebView webView,
            AuthService authService,
            UserService userService,
            WorkspaceController workspaceController
    ) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        this.authService = authService;
        this.userService = userService;
        this.workspaceService = workspaceService;
        initialize();
    }

    private void initialize() {

        webEngine.setOnAlert(e ->
                System.out.println("JS Alert: " + e.getData())
        );

        loadInitialPage();

        webEngine.getLoadWorker().stateProperty().addListener((obs, o, n) -> {
            System.out.println("WebEngine state: " + o + " -> " + n);

            if (n == Worker.State.SUCCEEDED) {
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

    private void exposeBridge() {
        JSObject window = (JSObject) webEngine.executeScript("window");

        if (javaBridge == null) {
            javaBridge = new JavaBridge(this, authService, userService, workspaceService);
        }

        window.setMember("javaBridge", javaBridge);
        window.setMember("java", this);

        System.out.println("✓ Java bridge exposed");
    }

    private void loadProjects() {
        try {
            Long userId = Session.getInstance().getUserId();

            System.out.println("📦 Loading dashboards for user " + userId);

            var dashboards =
                    workspaceService.getDashboardsForUser(userId.intValue());

            String json = new Gson().toJson(dashboards);

            webEngine.executeScript(
                    "window.currentUserId=" + userId + "; loadProjects(" + json + ")"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPage(String page) {
        Platform.runLater(() -> {
            String url = getClass()
                    .getResource("/ressources/view/" + page)
                    .toExternalForm();

            System.out.println("Loading page: " + url);
            webEngine.load(url);
        });
    }

    public void openWorkspace(int teamId) {
        loadPage("workspace.html");

        webEngine.getLoadWorker().stateProperty().addListener((o, old, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                try {
                    var dashboard = workspaceService.getDashboard(teamId);
                    String json = new Gson().toJson(dashboard);
                    webEngine.executeScript("loadDashboard(" + json + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void logout() {
        Session.getInstance().clear();
        loadPage("register.html");
    }
  // ==================================================
// NEW PROJECT PAGE
// ==================================================
public void openNewProject() {

    System.out.println("➕ Opening NEW PROJECT page");

    loadPage("project-form.html");
}

}
