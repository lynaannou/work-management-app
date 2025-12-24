package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.workspace.model.Workspace;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DaoWorkspace {

    private final Connection connection;

    public DaoWorkspace(Connection connection) {
        this.connection = connection;
    }

    // ==========================
    // FIND BY TEAM ID
    // ==========================
    public Optional<Workspace> findById(int teamId) {
        System.out.println("🗄️ [DaoWorkspace] findById team_id = " + teamId);

        String sql = "SELECT * FROM team WHERE team_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Workspace ws = mapRow(rs);
                    System.out.println("✅ Found team: " + ws.getTeamId() + " | " + ws.getName());
                    return Optional.of(ws);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error in findById");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // ==========================
    // FIND TEAMS BY USER (LEAD + MEMBER)
    // ==========================
    public List<Workspace> findByUser(int userId) throws SQLException {

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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        }

        System.out.println("📊 Total teams for user " + userId + " = " + result.size());
        return result;
    }

    // ==========================
    // CHECK USER IN WORKSPACE
    // ==========================
    public boolean isUserInWorkspace(Long userId, int workspaceId) throws SQLException {

        String sql = """
            SELECT 1
            FROM team_member
            WHERE user_id = ?
              AND team_id = ?
            LIMIT 1
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, workspaceId);

            ResultSet rs = ps.executeQuery();
            return rs.next();
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
