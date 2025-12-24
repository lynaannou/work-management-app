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

        // ✅ CRITICAL FIX: Verify userId is not null
        if (user == null) {
            throw new AuthenticationException("Login failed: User object is null");
        }

        if (user.getUserId() == null) {
            System.err.println("❌ CRITICAL ERROR: user.getUserId() is NULL after login!");
            System.err.println("User email: " + user.getEmail());
            System.err.println("User object: " + user);
            throw new AuthenticationException("Login failed: User ID is null. User may not exist in database.");
        }

        System.out.println("✅ User authenticated: ID = " + user.getUserId() + ", Email = " + user.getEmail());

        // create session token (UUID)
        String token = UUID.randomUUID().toString();

        // Get role ("LEAD" or "MEMBER")
        // ✅ FIX: Handle case where user is not in any team yet
        UserRole role;
        try {
            role = teamMemberService.getUserRole(user.getUserId());
            System.out.println("✅ User role retrieved: " + role);
        } catch (Exception e) {
            System.out.println("⚠️ User not in any team yet, defaulting to MEMBER role");
            role = UserRole.MEMBER;
        }

        // save session
        Session.getInstance().setUser(user, token, role);

        System.out.println("✅ Session created successfully");
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
        user.setEmail(email);

        // hash password
        String hashedPassword = hashStrategy.hashPassword(plainPassword);
        user.setPasswordHash(hashedPassword);

        // save user - THIS SHOULD SET THE userId ON THE USER OBJECT
        userDao.create(user);

        // ✅ VERIFY userId was set after creation
        if (user.getUserId() == null) {
            System.err.println("❌ WARNING: UserDao.create() did not set userId on User object!");
            // Try to fetch the user to get the ID
            User fetchedUser = userDao.findByEmail(email)
                    .orElseThrow(() -> new SQLException("User was created but cannot be found"));
            user.setUserId(fetchedUser.getUserId());
            System.out.println("✅ Retrieved userId from database: " + user.getUserId());
        }

        System.out.println("✅ User registered successfully: ID = " + user.getUserId());
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