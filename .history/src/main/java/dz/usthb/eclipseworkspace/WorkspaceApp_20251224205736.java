package dz.usthb.eclipseworkspace;

import com.google.gson.Gson;
import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.user.controller.MainController;
import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.SecurityService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.BCryptHashStrategy;
import dz.usthb.eclipseworkspace.user.util.EmailPasswordLogin;
import dz.usthb.eclipseworkspace.user.util.PasswordHashStrategy;
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

        // ---------- DB + Workspace wiring ----------
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

        // ---------- WebView ----------
        WebView webView = new WebView();
        engine = webView.getEngine();

        // ---------- AUTH / USER wiring ----------
        UserDao userDao = new UserDao();
        PasswordHashStrategy hashStrategy = new BCryptHashStrategy();
        EmailPasswordLogin loginStrategy =
                new EmailPasswordLogin(userDao, hashStrategy);

        TeamMemberDao teamMemberDao = new TeamMemberDaoJdbc();
        TeamMemberService teamMemberService =
                new TeamMemberService(teamMemberDao);

        AuthService authService =
                new AuthService(userDao, hashStrategy, loginStrategy, teamMemberService);

        UserService userService =
                new UserService(userDao, hashStrategy);

        // ---------- Controller (AUTH + NAVIGATION OWNER) ----------
        MainController mainController =
        new MainController(webView, authService, userService);

        mainController.setLoginSuccessListener(this::loadProjectsPage);


        stage.setScene(new Scene(webView, 1300, 750));
        stage.setTitle("ECLIPSEWORK");
        stage.show();
    }

    // ==================================================
    // PROJECTS PAGE (CALLED ONLY AFTER LOGIN)
    // ==================================================
    void loadProjectsPage() {

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
                    System.out.println("✅ projects.html loaded");

                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("java", WorkspaceApp.this);

                    try {
                        Long currentUserId = security.getCurrentUserId();

                        System.out.println("👤 userId = " + currentUserId);
                        System.out.println("👑 role  = " + security.getCurrentRole());

                        List<WorkspaceDashboard> dashboards =
                                workspaceService.getDashboardsForUser(currentUserId.intValue());

                        System.out.println("📊 Dashboards = " + dashboards.size());

                        String json = new Gson().toJson(dashboards);
                        engine.executeScript("window.currentUserId = " + currentUserId + ";");

                        engine.executeScript("loadProjects(" + json + ")");

                    } catch (Exception ex) {
                        System.err.println("❌ ERROR loading dashboards");
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

        System.out.println("📂 Opening workspace teamId=" + teamId);

        URL url = getClass().getResource("/ressources/view/workspace.html");
        engine.load(url.toExternalForm());

        engine.getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            WorkspaceDashboard dashboard =
                                    workspaceService.getDashboard(teamId);

                            String json = new Gson().toJson(dashboard);
                            engine.executeScript("loadDashboard(" + json + ")");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    // ==================================================
    // JS NAVIGATION
    // ==================================================
    public void goProjects() {
        loadProjectsPage();
    }

    public static void main(String[] args) {
        launch();
    }
}
