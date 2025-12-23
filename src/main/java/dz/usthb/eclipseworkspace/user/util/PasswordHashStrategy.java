package dz.usthb.eclipseworkspace.user.util;

public interface PasswordHashStrategy {
    String hashPassword(String password);

    boolean verifyPassword(String password,String hashedPassword);
}
