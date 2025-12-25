package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.workspace.model.Workspace;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DaoWorkspace {

    // ==========================
    // CREATE WORKSPACE / PROJECT / TEAM
    // ==========================
    public int create(String name, String description, long leadUserId) throws SQLException {

        System.out.println("🆕 [DaoWorkspace] Creating new workspace");
        System.out.println("    name = " + name);
        System.out.println("    leadUserId = " + leadUserId);

        String sql = """
            INSERT INTO team (
                name,
                description,
                lead_user_id,
                status,
                created_at,
                open_tasks_count,
                done_tasks_count,
                total_tasks_count
            )
            VALUES (?, ?, ?, 'ACTIVE', CURRENT_DATE, 0, 0, 0)
        """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setLong(3, leadUserId);

            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Creating workspace failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int teamId = keys.getInt(1);
                    System.out.println("✅ Workspace created with team_id = " + teamId);
                    return teamId;
                } else {
                    throw new SQLException("Creating workspace failed, no ID obtained.");
                }
            }
        }
    }

    // ==========================
    // FIND BY TEAM ID
    // ==========================
    public Optional<Workspace> findById(int teamId) {

        System.out.println("🗄️ [DaoWorkspace] findById team_id = " + teamId);

        String sql = "SELECT * FROM team WHERE team_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Workspace ws = mapRow(rs);
                    System.out.println(
                            "✅ Found team: id=" + ws.getTeamId()
                                    + ", name=" + ws.getName()
                    );
                    return Optional.of(ws);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ DaoWorkspace.findById FAILED");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // ==========================
    // FIND TEAMS BY USER (LEAD + MEMBER)
    // ==========================
    public List<Workspace> findByUser(int userId) {

        System.out.println("\n===============================");
        System.out.println("🧩 [DaoWorkspace] findByUser userId = " + userId);
        System.out.println("===============================");

        String sql = """
            SELECT DISTINCT t.*
            FROM team t
            LEFT JOIN team_member tm ON tm.team_id = t.team_id
            WHERE t.lead_user_id = ?
               OR tm.user_id = ?
            ORDER BY t.team_id
        """;

        List<Workspace> result = new ArrayList<>();

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workspace ws = mapRow(rs);
                    result.add(ws);

                    System.out.println(
                            "   ➜ Team loaded: [id=" + ws.getTeamId()
                                    + ", name=" + ws.getName() + "]"
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ DaoWorkspace.findByUser FAILED");
            e.printStackTrace();
        }

        System.out.println("📊 Total teams for user " + userId + " = " + result.size());
        return result;
    }

    // ==========================
    // CHECK USER IN WORKSPACE
    // ==========================
    public boolean isUserInWorkspace(Long userId, int workspaceId) {

        String sql = """
            SELECT 1
            FROM team_member
            WHERE user_id = ?
              AND team_id = ?
            LIMIT 1
        """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, userId);
            ps.setInt(2, workspaceId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("❌ DaoWorkspace.isUserInWorkspace FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // ==========================
    // ROW MAPPER (SINGLE SOURCE)
    // ==========================
    private Workspace mapRow(ResultSet rs) throws SQLException {
        return new Workspace(
                rs.getInt("team_id"),
                rs.getString("name"),
                rs.getDate("created_at"),
                rs.getInt("open_tasks_count"),
                rs.getInt("done_tasks_count"),
                rs.getInt("total_tasks_count")
        );
    }
}
