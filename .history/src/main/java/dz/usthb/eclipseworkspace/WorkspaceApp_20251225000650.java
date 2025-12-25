package dz.usthb.eclipseworkspace;

import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.user.controller.MainController;
import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.BCryptHashStrategy;
import dz.usthb.eclipseworkspace.user.util.EmailPasswordLogin;
import dz.usthb.eclipseworkspace.user.util.PasswordHashStrategy;
import dz.usthb.eclipseworkspace.workspace.controller.WorkspaceController;
import dz.usthb.eclipseworkspace.workspace.dao.DaoAppUser;
import dz.usthb.eclipseworkspace.workspace.dao.DaoTask;
import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardBuilder;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardDirector;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.sql.Connection;

public class WorkspaceApp extends Application {

    @Override
    public void start(Stage stage) {

        // ==========================
        // DATABASE CHECK (FAIL FAST)
        // ==========================
        try {
            System.out.println("🔌 Attempting DB connection...");
            Connection connection = DBConnection.getConnection();
            System.out.println("✅ DB connection OK");
        } catch (Exception e) {
            System.err.println("❌ DATABASE CONNECTION FAILED");
            e.printStackTrace();
            return; // STOP APP
        }

        // ==========================
        // WORKSPACE LAYER
        // ==========================
        WorkspaceService workspaceService =
                new WorkspaceService(
                        new DaoWorkspace(),
                        new DaoTask(),
                        new DaoAppUser(),
                        new WorkspaceDashboardDirector(),
                        new WorkspaceDashboardBuilder()
                );

        WorkspaceController workspaceController =
                new WorkspaceController(workspaceService);

        // ==========================
        // USER / AUTH LAYER
        // ==========================
        UserDao userDao = new UserDao();
        PasswordHashStrategy hash = new BCryptHashStrategy();
        EmailPasswordLogin login =
                new EmailPasswordLogin(userDao, hash);

        TeamMemberService teamMemberService =
                new TeamMemberService(new TeamMemberDaoJdbc());

        AuthService authService =
                new AuthService(userDao, hash, login, teamMemberService);

        UserService userService =
                new UserService(userDao, hash);

        // ==========================
        // UI / WEBVIEW
        // ==========================
        WebView webView = new WebView();

        new MainController(
                webView,
                authService,
                userService,
                workspaceService,
                workspaceController
        );

        stage.setScene(new Scene(webView, 1300, 750));
        stage.setTitle("ECLIPSEWORK");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
