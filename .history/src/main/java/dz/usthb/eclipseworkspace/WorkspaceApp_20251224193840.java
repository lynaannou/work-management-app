package dz.usthb.eclipseworkspace;

import com.google.gson.Gson;
import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.user.service.SecurityService;
import dz.usthb.eclipseworkspace.workspace.dao.DaoAppUser;
import dz.usthb.eclipseworkspace.workspace.dao.DaoTask;
import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardBuilder;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardDirector;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;
import java.sql.Connection;
import java.util.List;

public class WorkspaceApp extends Application {

    private WorkspaceService workspaceService;
    private WebEngine engine;

    private final SecurityService security = SecurityService.getInstance();

    @Override
    public void start(Stage stage) {

        System.out.println("\n===============================");
        System.out.println("🚀 STARTING WorkspaceApp");
        System.out.println("===============================");

        try {
            Connection connection = DBConnection.getConnection();
            System.out.println("✅ DB connection established");

            workspaceService = new WorkspaceService(
                    new DaoWorkspace(connection),
                    new DaoTask(connection),
                    new DaoAppUser(connection),
                    new WorkspaceDashboardDirector(),
                    new WorkspaceDashboardBuilder()
            );

        } catch (Exception e) {
            System.err.println("❌ FAILED TO INITIALIZE WorkspaceService");
            e.printStackTrace();
            return;
        }

        WebView webView = new WebView();
        engine = webView.getEngine();

        loadProjectsPage();

        stage.setScene(new Scene(webView, 1300, 750));
        stage.setTitle("ECLIPSEWORK");
        stage.show();
    }

    // ==================================================
    // PROJECTS PAGE
    // ==================================================

    private void loadProjectsPage() {

        System.out.println("\n===============================");
        System.out.println("📄 Loading PROJECTS page");
        System.out.println("===============================");

        security.requireAuthentication();

        System.out.println("🔐 Session OK");
        System.out.println("🔎 Security context: " + security.getSecurityContextSummary());

        URL url = getClass().getResource("/ressources/view/projects.html");
        System.out.println("🌐 Loading URL: " + url);

        engine.load(url.toExternalForm());

        ChangeListener<Worker.State> listener = new ChangeListener<>() {
            @Override
            public void changed(
                    ObservableValue<? extends Worker.State> obs,
                    Worker.State oldState,
                    Worker.State newState
            ) {
                if (newState == Worker.State.SUCCEEDED) {

                    engine.getLoadWorker().stateProperty().removeListener(this);

                    System.out.println("✅ projects.html loaded successfully");

                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("java", WorkspaceApp.this);
                    System.out.println("🔗 Java bridge injected into JS");

                    try {
                        Long currentUserId = security.getCurrentUserId();

                        System.out.println("👤 Current userId = " + currentUserId);
                        System.out.println("👑 Current role  = " + security.getCurrentRole());

                        List<WorkspaceDashboard> dashboards =
                                workspaceService.getDashboardsForUser(currentUserId.intValue());

                        System.out.println("📊 Dashboards returned = " + dashboards.size());

                        for (WorkspaceDashboard d : dashboards) {
                            System.out.println("   ➜ Dashboard for workspace: " + d.getWorkspace().getTeamId());
                        }

                        String json = new Gson().toJson(dashboards);
                        System.out.println("📤 Sending dashboards JSON to JS");

                        engine.executeScript("loadProjects(" + json + ")");

                    } catch (Exception ex) {
                        System.err.println("❌ ERROR while loading dashboards");
                        ex.printStackTrace();
                    }
                }
            }
        };

        engine.getLoadWorker().stateProperty().addListener(listener);
    }

    // ==================================================
    // WORKSPACE PAGE
    // ==================================================

    public void openWorkspace(int teamId) {

        System.out.println("\n===============================");
        System.out.println("📂 Opening WORKSPACE");
        System.out.println("===============================");
        System.out.println("➡️ teamId = " + teamId);

        URL url = getClass().getResource("/ressources/view/workspace.html");
        System.out.println("🌐 Loading URL: " + url);

        engine.load(url.toExternalForm());

        ChangeListener<Worker.State> listener = new ChangeListener<>() {
            @Override
            public void changed(
                    ObservableValue<? extends Worker.State> obs,
                    Worker.State oldState,
                    Worker.State newState
            ) {
                if (newState == Worker.State.SUCCEEDED) {

                    engine.getLoadWorker().stateProperty().removeListener(this);

                    System.out.println("✅ workspace.html loaded");

                    try {
                        System.out.println("🔐 Security context: " + security.getSecurityContextSummary());

                        WorkspaceDashboard dashboard =
                                workspaceService.getDashboard(teamId);

                        System.out.println("📊 Dashboard loaded for teamId = " + teamId);

                        String json = new Gson().toJson(dashboard);
                        engine.executeScript("loadDashboard(" + json + ")");

                    } catch (Exception ex) {
                        System.err.println("❌ ERROR while loading workspace dashboard");
                        ex.printStackTrace();
                    }
                }
            }
        };

        engine.getLoadWorker().stateProperty().addListener(listener);
    }

    // ==================================================
    // NAVIGATION FROM JS
    // ==================================================

    public void goProjects() {
        System.out.println("🔁 Navigation: goProjects()");
        loadProjectsPage();
    }

    public static void main(String[] args) {
        launch();
    }
}
