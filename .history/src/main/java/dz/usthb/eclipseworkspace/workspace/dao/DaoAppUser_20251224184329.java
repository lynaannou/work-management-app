package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoAppUser {

    private Connection connection;

    public DaoAppUser(Connection connection) {
        this.connection = connection;
    }

    public AppUser findById(int id) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) return map(rs);
        return null;
    }

    public AppUser findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM app_user WHERE email = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) return map(rs);
        return null;
    }

    public List<AppUser> findAll() throws SQLException {
        String sql = "SELECT * FROM app_user ORDER BY user_id";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<AppUser> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<AppUser> findMembersOfWorkspace(int teamId) throws SQLException {
        String sql = """
                SELECT u.*
                FROM team_member tm
                JOIN app_user u ON u.user_id = tm.user_id
                WHERE tm.team_id = ?
                """;

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, teamId);

        ResultSet rs = ps.executeQuery();
        List<AppUser> list = new ArrayList<>();

        while (rs.next()) list.add(map(rs));
        return list;
    }

    public void insert(AppUser u) throws SQLException {
        String sql = """
                INSERT INTO app_user(email, username, first_name, last_name, phone, password_hash)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, u.getEmail());
        ps.setString(2, u.getUsername());
        ps.setString(3, u.getFirstName());
        ps.setString(4, u.getLastName());
        ps.setString(5, u.getPhone());
        ps.setString(6, u.getPasswordHash());

        ps.executeUpdate();
    }

    private AppUser map(ResultSet rs) throws SQLException {
        return new AppUser(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("password_hash"),
                rs.getDate("created_at")
        );
    }
}
