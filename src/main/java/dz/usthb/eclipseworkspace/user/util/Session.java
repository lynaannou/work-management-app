package dz.usthb.eclipseworkspace.user.util;

import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.UserRole;

public class Session {

    private static Session instance;

    private User currentUser;
    private String token;
    private UserRole role;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(User user, String token, UserRole role) {
        this.currentUser = user;
        this.token = token;
        this.role = role;
    }
    public Long getUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }

    public boolean isAuthenticated() {
        return currentUser != null && token != null;
    }

    public boolean isLead() {
        return role == UserRole.LEAD;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public UserRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public void clear() {
        currentUser = null;
        token = null;
        role = null;
    }
}
