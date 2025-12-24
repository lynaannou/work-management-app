package dz.usthb.eclipseworkspace.user.service;

import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.exception.AuthorizationException;
import dz.usthb.eclipseworkspace.user.util.Session;
import dz.usthb.eclipseworkspace.user.util.UserRole;

/**
 * Centralized Security Service - Single Source of Truth for Authentication/Authorization
 *
 * This service encapsulates all security logic to avoid code repetition.
 * Every service should use this instead of directly checking Session.
 *
 * Benefits:
 * - DRY (Don't Repeat Yourself) principle
 * - Single Responsibility Principle
 * - Easy to maintain and test
 * - Consistent security across the application
 *
 * Usage:
 * <pre>
 * public class MyService {
 *     private final SecurityService security = SecurityService.getInstance();
 *
 *     public void myMethod() {
 *         security.requireAuthentication();
 *         // business logic...
 *     }
 * }
 * </pre>
 */
public class SecurityService {

    private static SecurityService instance;

    private SecurityService() {
        // Private constructor for Singleton pattern
    }

    /**
     * Get singleton instance
     */
    public static SecurityService getInstance() {
        if (instance == null) {
            instance = new SecurityService();
        }
        return instance;
    }

    // ==========================================
    // AUTHENTICATION CHECKS
    // ==========================================

    /**
     * Verify user is authenticated.
     * This is the most common check - use it in every protected method.
     *
     * @throws AuthenticationException if user is not authenticated
     */
    public void requireAuthentication() {
        Session session = Session.getInstance();

        if (!session.isAuthenticated()) {
            throw new AuthenticationException("User must be authenticated to access this resource");
        }

        if (session.getUserId() == null) {
            throw new AuthenticationException("Invalid session - no user ID found");
        }
    }

    /**
     * Get current authenticated user ID.
     * Automatically checks authentication first.
     *
     * @return current user ID
     * @throws AuthenticationException if not authenticated
     */
    public Long getCurrentUserId() {
        requireAuthentication();
        return Session.getInstance().getUserId();
    }

    /**
     * Get current user role.
     * Automatically checks authentication first.
     *
     * @return current user role
     * @throws AuthenticationException if not authenticated
     */
    public UserRole getCurrentRole() {
        requireAuthentication();
        return Session.getInstance().getRole();
    }

    /**
     * Check if user is authenticated (no exception thrown).
     * Use this for conditional logic, not for security checks.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Session session = Session.getInstance();
        return session.isAuthenticated() && session.getUserId() != null;
    }

    // ==========================================
    // ROLE-BASED AUTHORIZATION
    // ==========================================

    /**
     * Verify user has a specific role.
     *
     * @param requiredRole the role required for the operation
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if user doesn't have the required role
     */
    public void requireRole(UserRole requiredRole) {
        requireAuthentication();

        UserRole currentRole = Session.getInstance().getRole();

        if (currentRole != requiredRole) {
            throw new AuthorizationException(
                    String.format("Operation requires %s role, but user has %s role",
                            requiredRole, currentRole)
            );
        }
    }

    /**
     * Verify user is a team LEAD.
     * Shorthand for requireRole(UserRole.LEAD).
     *
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if user is not a LEAD
     */
    public void requireLead() {
        requireRole(UserRole.LEAD);
    }

    /**
     * Verify user is a MEMBER.
     * Shorthand for requireRole(UserRole.MEMBER).
     *
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if user is not a MEMBER
     */
    public void requireMember() {
        requireRole(UserRole.MEMBER);
    }

    /**
     * Check if current user is a LEAD (no exception).
     * Use for conditional logic, not for security.
     *
     * @return true if user is LEAD, false otherwise
     */
    public boolean isLead() {
        try {
            requireAuthentication();
            return Session.getInstance().getRole() == UserRole.LEAD;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    /**
     * Check if current user is a MEMBER (no exception).
     *
     * @return true if user is MEMBER, false otherwise
     */
    public boolean isMember() {
        try {
            requireAuthentication();
            return Session.getInstance().getRole() == UserRole.MEMBER;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    // ==========================================
    // OWNERSHIP CHECKS
    // ==========================================

    /**
     * Verify current user owns a resource.
     * Use this when only the resource owner can access/modify it.
     *
     * @param resourceOwnerId the ID of the resource owner
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if user is not the owner
     */
    public void requireOwnership(Long resourceOwnerId) {
        requireAuthentication();

        Long currentUserId = getCurrentUserId();

        if (!currentUserId.equals(resourceOwnerId)) {
            throw new AuthorizationException("You don't have permission to access this resource");
        }
    }

    /**
     * Verify current user owns resource OR is a LEAD.
     * Use this when LEADs can manage anyone's resources.
     *
     * @param resourceOwnerId the ID of the resource owner
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if user is neither owner nor LEAD
     */
    public void requireOwnershipOrLead(Long resourceOwnerId) {
        requireAuthentication();

        Long currentUserId = getCurrentUserId();

        // Check if user is the owner
        if (currentUserId.equals(resourceOwnerId)) {
            return;
        }

        // Check if user is a LEAD
        if (Session.getInstance().getRole() == UserRole.LEAD) {
            return;
        }

        throw new AuthorizationException(
                "You don't have permission to access this resource. Must be owner or team lead."
        );
    }

    // ==========================================
    // USER ACCESS CHECKS
    // ==========================================

    /**
     * Verify user can view another user's data.
     * Users can view their own data, LEADs can view anyone's data.
     *
     * @param targetUserId the ID of the user whose data is being accessed
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if access is not permitted
     */
    public void requireCanViewUser(Long targetUserId) {
        requireAuthentication();

        Long currentUserId = getCurrentUserId();

        // Users can view their own data
        if (currentUserId.equals(targetUserId)) {
            return;
        }

        // LEADs can view any user's data
        if (Session.getInstance().getRole() == UserRole.LEAD) {
            return;
        }

        throw new AuthorizationException(
                "You don't have permission to view this user's data"
        );
    }

    /**
     * Verify user can modify another user's data.
     * Users can modify their own data, only LEADs can modify others' data.
     *
     * @param targetUserId the ID of the user whose data is being modified
     * @throws AuthenticationException if not authenticated
     * @throws AuthorizationException if modification is not permitted
     */
    public void requireCanModifyUser(Long targetUserId) {
        requireAuthentication();

        Long currentUserId = getCurrentUserId();

        // Users can modify their own data
        if (currentUserId.equals(targetUserId)) {
            return;
        }

        // Only LEADs can modify other users' data
        if (Session.getInstance().getRole() != UserRole.LEAD) {
            throw new AuthorizationException(
                    "Only team leads can modify other users' data"
            );
        }
    }

    // ==========================================
    // TOKEN VALIDATION
    // ==========================================

    /**
     * Verify a token is valid and matches current session.
     * Use this when token is passed explicitly (e.g., from frontend).
     *
     * @param token the token to verify
     * @throws AuthenticationException if token is invalid
     */
    public void requireValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new AuthenticationException("Authentication token is required");
        }

        Session session = Session.getInstance();

        if (!token.equals(session.getToken())) {
            throw new AuthenticationException("Invalid or expired token");
        }

        requireAuthentication();
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    /**
     * Log security check (useful for debugging).
     *
     * @param checkType type of check performed
     * @param success whether check was successful
     */
    private void logSecurityCheck(String checkType, boolean success) {
        String status = success ? "✅ PASSED" : "❌ FAILED";
        System.out.println(String.format("[SECURITY] %s - %s", checkType, status));
    }

    /**
     * Get a summary of current security context.
     * Useful for debugging.
     *
     * @return security context summary
     */
    public String getSecurityContextSummary() {
        if (!isAuthenticated()) {
            return "Not authenticated";
        }

        Session session = Session.getInstance();
        return String.format(
                "User ID: %d, Role: %s, Token: %s",
                session.getUserId(),
                session.getRole(),
                session.getToken() != null ? "present" : "absent"
        );
    }
}