package dz.usthb.eclipseworkspace;

import com.google.gson.Gson;
import dz.usthb.eclipseworkspace.config.DBConnection;
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

public class WorkspaceApp extends Application {

    private WorkspaceService workspaceService;
    private WebEngine engine;

    // 🔧 TEMP: simulate logged-in user
    private static final int CURRENT_USER_ID = 9;

    @Override
    public void start(Stage stage) {

        try {
            Connection connection = DBConnection.getConnection();

            workspaceService = new WorkspaceService(
                    new DaoWorkspace(connection),
                    new DaoTask(connection),
                    new DaoAppUser(connection),
                    new WorkspaceDashboardDirector(),
                    new WorkspaceDashboardBuilder()
            );

        } catch (Exception e) {
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
        URL url = getClass().getResource("/ressources/view/projects.html");
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

                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("java", WorkspaceApp.this);

                    try {
                        var dashboards =
                                workspaceService.getDashboardsForUser(CURRENT_USER_ID);

                        String json = new Gson().toJson(dashboards);
                        engine.executeScript("loadProjects(" + json + ")");
                    } catch (Exception ex) {
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
        URL url = getClass().getResource("/ressources/view/workspace.html");
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

                    try {
                        WorkspaceDashboard dashboard =
                                workspaceService.getDashboard(teamId);

                        String json = new Gson().toJson(dashboard);
                        engine.executeScript("loadDashboard(" + json + ")");
                    } catch (Exception ex) {
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
        loadProjectsPage();
    }

    public static void main(String[] args) {
        launch();
    }
}
