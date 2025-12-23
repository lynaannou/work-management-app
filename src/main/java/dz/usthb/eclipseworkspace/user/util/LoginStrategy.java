package dz.usthb.eclipseworkspace.user.util;

import dz.usthb.eclipseworkspace.user.model.User;

import java.sql.SQLException;

public interface LoginStrategy {
    User login(String identifier, String secret) throws SQLException;
}

