package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.service.UserService;
import dz.usthb.eclipseworkspace.user.util.BCryptHashStrategy;
import dz.usthb.eclipseworkspace.user.util.EmailPasswordLogin;
import dz.usthb.eclipseworkspace.user.util.PasswordHashStrategy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class test {

    
    public void start(Stage stage) {

        // ---- WebView (one instance for the whole app) ----
        WebView webView = new WebView();

        // ---- Backend wiring (manual DI for now) ----
        UserDao userDao = new UserDao();
        PasswordHashStrategy hashStrategy = new BCryptHashStrategy();
        EmailPasswordLogin loginStrategy = new EmailPasswordLogin(userDao, hashStrategy);

        // ---- TeamMemberService for role retrieval ----
        TeamMemberDao teamMemberDao = new TeamMemberDaoJdbc();
        TeamMemberService teamMemberService = new TeamMemberService(teamMemberDao);

        // ---- AuthService with TeamMemberService ----
        AuthService authService = new AuthService(userDao, hashStrategy, loginStrategy, teamMemberService);

        // ---- Controller ----
        new MainController(webView, authService,new UserService(userDao,hashStrategy));

        // ---- Scene ----
        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("EclipseWork Management");
        stage.setScene(scene);
        stage.show();
    }

}
