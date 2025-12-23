package dz.usthb.eclipseworkspace.user.controller;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.user.service.AuthService;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.UserRole;
import javafx.scene.web.WebEngine;

public class JavaAuthBridge {

    private final AuthService authService;
    private final WebEngine webEngine;
    private final TeamMemberDao teamMemberDao;

    public JavaAuthBridge(AuthService authService, WebEngine webEngine) {
        this.authService = authService;
        this.webEngine = webEngine;
        this.teamMemberDao = new TeamMemberDaoJdbc();
    }

    // Called from JS: login(email, password)
    public String login(String email, String password) {
        try {
            email = email.trim().toLowerCase();
            String token = authService.login(email, password);
            User user = authService.verifyToken(token);

            // Get role from DB as string ("LEAD" or "MEMBER")
            String dbRole = teamMemberDao.getRoleByUserId(user.getUserId());

            // Convert String to enum (case-insensitive)
            UserRole role = dbRole != null ? UserRole.valueOf(dbRole.toUpperCase()) : UserRole.MEMBER;

        // Save in session
            Session.getInstance().setUser(user, token, role);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // Called from JS: logout()
    public void logout() {
        Session session = Session.getInstance();
        if (session.getToken() != null) {
            authService.logout(session.getToken());
        }
        session.clear();
    }

    // Called from JS: register(...)
    public String register(String email, String firstName, String lastName, String password) {
        try {
            User user = new User();
            user.setEmail(email.trim());
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());

            authService.register(user, password.trim());
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // Called from JS to load another HTML page inside WebView
    public void loadPage(String pagePath) {
        String url = getClass().getResource("/ressources/view/" + pagePath).toExternalForm();
        webEngine.load(url);
    }

    // Helper to expose to JS
    public Object getJSObject() {
        return this;
    }
}

