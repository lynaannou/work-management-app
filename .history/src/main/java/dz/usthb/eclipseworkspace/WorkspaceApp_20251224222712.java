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

    @Override
    public void start(Stage stage) {
        Connection connection;

          try {
        System.out.println("🔌 Attempting DB connection...");
        connection = DBConnection.getConnection();
        System.out.println("✅ DB connection OK");

    } catch (Exception e) {
        System.err.println("❌ DATABASE CONNECTION FAILED");
        e.printStackTrace();
        return; // STOP APP if DB fails
    }

        WorkspaceService workspaceService =
                new WorkspaceService(
                        new DaoWorkspace(),
                        new DaoTask(),
                        new DaoAppUser(),
                        new WorkspaceDashboardDirector(),
                        new WorkspaceDashboardBuilder()
                );

        WebView webView = new WebView();

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

        new MainController(
                webView,
                authService,
                userService,
                workspaceService
        );

        stage.setScene(new Scene(webView, 1300, 750));
        stage.setTitle("ECLIPSEWORK");
        stage.show();
    }
    // ==================================================
// NEW PROJECT PAGE
// ==================================================
public void openNewProject() {

    System.out.println("➕ Opening NEW PROJECT page");

    URL url = getClass().getResource("/ressources/view/project-form.html");
    if (url == null) {
        System.err.println("❌ project-form.html not found");
        return;
    }

    engine.load(url.toExternalForm());
}


    public static void main(String[] args) {
        launch();
    }
}
