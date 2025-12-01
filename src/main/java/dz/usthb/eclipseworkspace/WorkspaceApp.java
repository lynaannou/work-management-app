package dz.usthb.eclipseworkspace;

import com.google.gson.Gson;

import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.workspace.dao.DaoTask;
import dz.usthb.eclipseworkspace.workspace.dao.DaoAppUser;

import dz.usthb.eclipseworkspace.config.DBConnection;

import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardBuilder;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardDirector;

import java.net.URL;
import java.sql.Connection;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WorkspaceApp extends Application {

    private WorkspaceService workspaceService;

    @Override
    public void start(Stage stage) {

        System.out.println("=== WORKSPACE APP STARTING ===");

        try {
            // 1. DB CONNECTION
            Connection connection = DBConnection.getConnection();
            System.out.println("DEBUG: DB connection success: " + (connection != null));

            // 2. DAO SETUP
            DaoWorkspace daoWorkspace = new DaoWorkspace(connection);
            DaoTask daoTask = new DaoTask(connection);
            DaoAppUser daoAppUser = new DaoAppUser(connection);

            // 3. BUILDER + DIRECTOR
            WorkspaceDashboardBuilder builder = new WorkspaceDashboardBuilder();
            WorkspaceDashboardDirector director = new WorkspaceDashboardDirector();

            // 4. SERVICE
            workspaceService = new WorkspaceService(
                    daoWorkspace,
                    daoTask,
                    daoAppUser,
                    director,
                    builder
            );

            System.out.println("DEBUG: WorkspaceService created.");

        } catch (Exception e) {
            System.out.println("FATAL ERROR INITIALIZING APPLICATION:");
            e.printStackTrace();
            return;
        }

        // 5. SETUP WEBVIEW
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        URL url = getClass().getResource("/ressources/view/workspace.html");
        System.out.println("DEBUG: HTML Resource URL = " + url);

        engine.load(url.toExternalForm());

        stage.setScene(new Scene(webView, 1300, 600));
        stage.setTitle("ECLIPSEWORK Dashboard");
        stage.show();

        // 6. LOAD DASHBOARD DATA WHEN PAGE IS READY
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {

                System.out.println("DEBUG: HTML page fully loaded.");

                try {
                    System.out.println("DEBUG: Calling workspaceService.getDashboard(1)...");
                    WorkspaceDashboard dashboard = workspaceService.getDashboard(1);

                    if (dashboard == null) {
                        System.out.println("ERROR: Dashboard returned null!");
                        return;
                    }

                    Gson gson = new Gson();
                    String json = gson.toJson(dashboard);

                    System.out.println("=== JSON SENT TO JAVASCRIPT ===");
                    System.out.println(json);
                    System.out.println("================================");

                    engine.executeScript("loadDashboard(" + json + ");");

                } catch (Exception e) {
                    System.out.println("ERROR loading dashboard:");
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
