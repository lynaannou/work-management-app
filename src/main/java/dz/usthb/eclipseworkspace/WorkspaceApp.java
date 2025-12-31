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
import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardBuilder;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardDirector;
import dz.usthb.eclipseworkspace.todo.controller.TodoController;
import dz.usthb.eclipseworkspace.todo.dao.DaoTodoTask;
import dz.usthb.eclipseworkspace.task.service.TaskService;
import dz.usthb.eclipseworkspace.task.controller.TaskController;
import dz.usthb.eclipseworkspace.task.dao.DaoTask;
import dz.usthb.eclipseworkspace.task.dao.DaoTaskJdbc;

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
        Connection connection;
        try {
            System.out.println("🔌 Attempting DB connection...");
            connection = DBConnection.getConnection();
            System.out.println("✅ DB connection OK");
        } catch (Exception e) {
            System.err.println("❌ DATABASE CONNECTION FAILED");
            e.printStackTrace();
            return; // STOP APP
        }

        // ==========================
        // TASK DAO (DECLARE FIRST)
        // ==========================
        DaoTask daoTask = new DaoTaskJdbc();

        // ==========================
        // WORKSPACE LAYER
        // ==========================
        WorkspaceService workspaceService =
                new WorkspaceService(
                        new DaoWorkspace(),
                        daoTask,   // ✅ now valid
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
        // TASK SERVICE + CONTROLLER
        // ==========================
        TaskService taskService =
                new TaskService(daoTask);

        TaskController taskController =
                new TaskController(taskService);

        // ==========================
        // TODO SERVICE + CONTROLLER
        // ==========================
        TodoController todoController =
                new TodoController(new DaoTodoTask(connection));

        // ==========================
        // UI
        // ==========================
        WebView webView = new WebView();

        new MainController(
                webView,
                authService,
                userService,
                workspaceService,
                workspaceController,
                todoController,
                taskController
        );

        stage.setScene(new Scene(webView, 1300, 750));
        stage.setTitle("ECLIPSEWORK");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
