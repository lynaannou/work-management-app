package dz.usthb.eclipseworkspace.user.service;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.model.User;
import dz.usthb.eclipseworkspace.user.util.LoginStrategy;
import dz.usthb.eclipseworkspace.user.util.PasswordHashStrategy;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.util.UserRole;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {

    private final UserDao userDao;
    private final PasswordHashStrategy hashStrategy;
    private final LoginStrategy loginStrategy;
    private final TeamMemberService teamMemberService;

    public AuthService(UserDao userDao, PasswordHashStrategy hashStrategy,
                       LoginStrategy loginStrategy, TeamMemberService teamMemberService) {
        this.userDao = userDao;
        this.hashStrategy = hashStrategy;
        this.loginStrategy = loginStrategy;
        this.teamMemberService = teamMemberService;
    }


    // Login user
    public String login(String email, String password) throws SQLException, Exception {
        User user = loginStrategy.login(email, password); // throws AuthenticationException if invalid

        // create session token (UUID)
        String token = UUID.randomUUID().toString();

        // Get role ("LEAD" or "MEMBER")
        UserRole role = teamMemberService.getUserRole(user.getUserId());

        // save session
        Session.getInstance().setUser(user, token,role);

        return token; // return token to caller (frontend/JavaFX)
    }

    // Register new user
    public User register(User user, String plainPassword) throws SQLException {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (plainPassword == null || plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        String email = user.getEmail().trim().toLowerCase();

        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        // normalize email
        user.setEmail(user.getEmail().trim().toLowerCase());

        // hash password
        String hashedPassword = hashStrategy.hashPassword(plainPassword);
        user.setPasswordHash(hashedPassword);

        // save user
        userDao.create(user);

        return user;
    }

    // VERIFY SESSION
    public User verifyToken(String token) throws AuthenticationException {
        Session session = Session.getInstance();

        // Check if session exists and token matches
        if (session.getToken() == null || !session.getToken().equals(token)) {
            throw new AuthenticationException("Invalid or expired session");
        }

        User user = session.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("User not found for this session");
        }

        return user;
    }


    // LOGOUT
    public void logout(String token) {
        Session.getInstance().clear();
    }

}

