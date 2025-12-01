package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.config.DBConnection;
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

                    Workspace workspace = new Workspace(
                            rs.getInt("team_id"),
                            rs.getString("name"),
                            rs.getDate("created_at"),
                            rs.getInt("open_tasks_count"),
                            rs.getInt("done_tasks_count"),
                            rs.getInt("total_tasks_count")
                    );

                    // Later: workspace.setTasks(fetchTasksForTeam(teamId));
                    return Optional.of(workspace);
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

                Workspace w = new Workspace(
                        rs.getInt("team_id"),
                        rs.getString("name"),
                        rs.getDate("created_at"),
                        rs.getInt("open_tasks_count"),
                        rs.getInt("done_tasks_count"),
                        rs.getInt("total_tasks_count")
                );

                list.add(w);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void save(Workspace workspace) {

        String sql = "INSERT INTO team (team_id, name, created_at, open_tasks_count, done_tasks_count, total_tasks_count) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, workspace.getTeamId());
            ps.setString(2, workspace.getName());
            ps.setDate(3, workspace.getCreatedAt());
            ps.setInt(4, workspace.getOpenTasksCount());
            ps.setInt(5, workspace.getDoneTasksCount());
            ps.setInt(6, workspace.getTotalTasksCount());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Workspace workspace) {

        String sql = "UPDATE team SET name = ?, created_at = ?, open_tasks_count = ?, " +
                "done_tasks_count = ?, total_tasks_count = ? WHERE team_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, workspace.getName());
            ps.setDate(2, workspace.getCreatedAt());
            ps.setInt(3, workspace.getOpenTasksCount());
            ps.setInt(4, workspace.getDoneTasksCount());
            ps.setInt(5, workspace.getTotalTasksCount());
            ps.setInt(6, workspace.getTeamId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Workspace workspace) {

        String sql = "DELETE FROM team WHERE team_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, workspace.getTeamId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 
     * Later you'll implement this to fetch real tasks 
     */
    private List<Task> fetchTasksForTeam(int teamId) {
        return new ArrayList<>();
    }
}
