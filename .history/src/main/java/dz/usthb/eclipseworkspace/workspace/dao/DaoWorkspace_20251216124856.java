package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DaoWorkspace {

    private final Connection connection;

    public DaoWorkspace(Connection connection) {
        this.connection = connection;
    }

    public Optional<Workspace> findById(int teamId) {

        String sql = "SELECT * FROM team WHERE team_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public List<Workspace> getAll() {

        List<Workspace> list = new ArrayList<>();
        String sql = "SELECT * FROM team ORDER BY team_id";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Workspace> findByUser(int userId) throws SQLException {

        String sql = """
            SELECT DISTINCT t.*
            FROM team t
            JOIN team_member tm ON tm.team_id = t.team_id
            WHERE tm.user_id = ?
        """;

        List<Workspace> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }

        return result;
    }

    // ✅ THIS WAS MISSING
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

    /** Later */
    private List<Task> fetchTasksForTeam(int teamId) {
        return new ArrayList<>();
    }
}
