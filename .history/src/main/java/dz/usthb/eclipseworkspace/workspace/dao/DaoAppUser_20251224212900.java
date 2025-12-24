package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoAppUser {

    // ==========================
    // FIND BY ID
    // ==========================
    public AppUser findById(int id) throws SQLException {

        String sql = "SELECT * FROM app_user WHERE user_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }

        return null;
    }

    // ==========================
    // FIND BY EMAIL
    // ==========================
    public AppUser findByEmail(String email) throws SQLException {

        String sql = "SELECT * FROM app_user WHERE email = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }

        return null;
    }

    // ==========================
    // FIND ALL
    // ==========================
    public List<AppUser> findAll() throws SQLException {

        String sql = "SELECT * FROM app_user ORDER BY user_id";
        List<AppUser> list = new ArrayList<>();

        try (
            Connection connection = DBConnection.getConnection();
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql)
        ) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    // ==========================
    // FIND MEMBERS OF WORKSPACE
    // ==========================
    public List<AppUser> findMembersOfWorkspace(int teamId) throws SQLException {

        System.out.println("👥 [DaoAppUser] findMembersOfWorkspace teamId=" + teamId);

        String sql = """
            SELECT u.*
            FROM team_member tm
            JOIN app_user u ON u.user_id = tm.user_id
            WHERE tm.team_id = ?
        """;

        List<AppUser> list = new ArrayList<>();

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AppUser u = map(rs);
                    list.add(u);

                    System.out.println(
                        "   ➜ Member: " + u.getUser_id()
                        + " " + u.getFirstName() + " " + u.getLastName()
                    );
                }
            }
        }

        return list;
    }

    // ==========================
    // INSERT USER
    // ==========================
    public void insert(AppUser u) throws SQLException {

        String sql = """
            INSERT INTO app_user(
                email,
                username,
                first_name,
                last_name,
                phone,
                password_hash
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getFirstName());
            ps.setString(4, u.getLastName());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getPasswordHash());

            ps.executeUpdate();
            System.out.println("✅ User inserted: " + u.getEmail());
        }
    }

    // ==========================
    // ROW MAPPER
    // ==========================
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
