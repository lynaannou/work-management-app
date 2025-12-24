package dz.usthb.eclipseworkspace.user.exception;

/**
 * Thrown when user lacks permission for an operation.
 *
 * This is different from AuthenticationException:
 * - AuthenticationException: User identity cannot be verified (not logged in)
 * - AuthorizationException: User identity is known but lacks permission (forbidden)
 *
 * HTTP equivalent:
 * - AuthenticationException → 401 Unauthorized
 * - AuthorizationException → 403 Forbidden
 */
public class AuthorizationException extends RuntimeException {

    /**
     * Create exception with message
     *
     * @param message description of the authorization failure
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Create exception with message and cause
     *
     * @param message description of the authorization failure
     * @param cause the underlying cause
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
