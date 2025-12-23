package dz.usthb.eclipseworkspace.user.service;

import dz.usthb.eclipseworkspace.user.dao.UserDao;
import dz.usthb.eclipseworkspace.user.exception.UserNotFoundException;
import dz.usthb.eclipseworkspace.user.model.User;

import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUserById(Long id) throws SQLException {
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) throws SQLException {
        return userDao.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    public void updateUser(User user) throws SQLException {
        userDao.update(user);
    }

    public void deleteUser(Long id) throws SQLException {
        //userDao.delete(id);
    }
}
