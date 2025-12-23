package dz.usthb.eclipseworkspace.user.util;

import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.exception.AuthenticationException;
import dz.usthb.eclipseworkspace.user.model.User;

import java.sql.SQLException;

public class EmailPasswordLogin implements LoginStrategy{
    private final UserDao userDao;
    private final PasswordHashStrategy hashStrategy;

    public EmailPasswordLogin(UserDao userDao, PasswordHashStrategy hashStrategy) {
        this.userDao = userDao;
        this.hashStrategy = hashStrategy;
    }

    @Override
    public User login(String email, String password) throws SQLException {
        email = email.trim().toLowerCase();

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));


        boolean valid = hashStrategy.verifyPassword(password, user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid email or password");
        }

        return user;
    }
}
