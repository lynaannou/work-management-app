package dz.usthb.eclipseworkspace.user.util;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptHashStrategy implements PasswordHashStrategy{
    private final int WorkFactor = 10; //determines how many rounds of hashing will be performed

    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password,BCrypt.gensalt(WorkFactor));
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        // User input validation
        if (password == null || password.isBlank()) {
            return false;
        }

        // System data validation (this is not the user's fault)
        if (hashedPassword == null || hashedPassword.isBlank()) {
            throw new IllegalStateException("User password hash is missing");
        }

        // Password verification
        return BCrypt.checkpw(password, hashedPassword);
    }

}
