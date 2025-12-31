package dz.usthb.eclipseworkspace.task.dao;

import dz.usthb.eclipseworkspace.config.DBConnection;
import dz.usthb.eclipseworkspace.task.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DaoTaskJdbc implements DaoTask {

    private final Connection connection;

    public DaoTaskJdbc() {
        try {
            this.connection = DBConnection.getConnection();
            System.out.println("🟥 [DaoTaskJdbc] DB connection acquired");
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'obtenir la connexion DB", e);
        }
    }

    // =================================================
    // CREATE
    // =================================================
    @Override
    public void create(Task task) {

        String sql = """
            INSERT INTO task
            (team_id, team_member_id, title, description, status,
             progress_pct, start_date, due_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, task.getTeamId());

            if (task.getAssigneeId() == null || task.getAssigneeId() <= 0) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, task.getAssigneeId());
            }

            ps.setString(3, task.getTitle());
            ps.setString(4, task.getDescription());
            ps.setString(5, task.getStatus());
            ps.setInt(6, task.getProgressPct());
            ps.setObject(7, task.getStartDate());
            ps.setObject(8, task.getDueDate());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la tâche", e);
        }
    }

    // =================================================
    // PARTIAL UPDATES (SAFE)
    // =================================================

    public void updateStatusOnly(int taskId, String status) {

        String sql = """
            UPDATE task
            SET status = ?,
                completed_at = CASE
                    WHEN ? = 'DONE' THEN CURRENT_DATE
                    ELSE NULL
                END
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update status", e);
        }
    }

    public void updateTitleOnly(int taskId, String title) {

        String sql = """
            UPDATE task
            SET title = ?
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update title", e);
        }
    }

    public void updateDescriptionOnly(int taskId, String description) {

        String sql = """
            UPDATE task
            SET description = ?
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, description);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update description", e);
        }
    }

    public void updateDueDateOnly(int taskId, LocalDate dueDate) {

        String sql = """
            UPDATE task
            SET due_date = ?
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, dueDate);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update due date", e);
        }
    }

    // =================================================
    // FULL UPDATE
    // =================================================
    @Override
    public void update(Task task) {

        String sql = """
            UPDATE task
            SET title = ?,
                description = ?,
                status = ?,
                progress_pct = ?,
                start_date = ?,
                due_date = ?,
                team_member_id = ?
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus());
            ps.setInt(4, task.getProgressPct());
            ps.setObject(5, task.getStartDate());
            ps.setObject(6, task.getDueDate());

            if (task.getAssigneeId() == null || task.getAssigneeId() <= 0) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, task.getAssigneeId());
            }

            ps.setInt(8, task.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur update tâche", e);
        }
    }

    // =================================================
    // SOFT DELETE – ALL TASKS OF A TEAM (🔥 IMPORTANT)
    // =================================================
    @Override
    public void deleteByTeam(int teamId) {

        System.out.println("🧹 [DaoTaskJdbc] soft-deleteByTeam teamId=" + teamId);

        String sql = """
            UPDATE task
            SET is_deleted = TRUE,
                deleted_at = NOW()
            WHERE team_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur soft delete tâches du projet", e);
        }
    }

    // =================================================
    // FIND BY ID
    // =================================================
    @Override
    public Task findById(int taskId) {

        String sql = """
            SELECT *
            FROM task
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToTask(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur find task", e);
        }
    }

    // =================================================
    // FIND BY TEAM (🔥 VISIBILITY FIX)
    // =================================================
    @Override
    public List<Task> findByTeam(int teamId) {

        List<Task> tasks = new ArrayList<>();

        String sql = """
            SELECT *
            FROM task
            WHERE team_id = ?
              AND is_deleted = FALSE
            ORDER BY due_date
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur listing tâches", e);
        }

        return tasks;
    }

    // =================================================
    // SOFT DELETE – SINGLE TASK
    // =================================================
    @Override
    public void delete(int taskId) {

        String sql = """
            UPDATE task
            SET is_deleted = TRUE,
                deleted_at = NOW()
            WHERE task_id = ?
              AND is_deleted = FALSE
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur soft delete tâche", e);
        }
    }

    // =================================================
    // ROW MAPPING
    // =================================================
    private Task mapRowToTask(ResultSet rs) throws SQLException {

        Task task = new Task();

        task.setId(rs.getInt("task_id"));
        task.setTeamId(rs.getInt("team_id"));

        int assigneeId = rs.getInt("team_member_id");
        task.setAssigneeId(rs.wasNull() ? null : assigneeId);

        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setProgressPct(rs.getInt("progress_pct"));
        task.setStartDate(rs.getObject("start_date", LocalDate.class));
        task.setDueDate(rs.getObject("due_date", LocalDate.class));
        task.setCreatedAt(rs.getObject("created_at", LocalDate.class));
        task.setCompletedAt(rs.getObject("completed_at", LocalDate.class));

        String status = rs.getString("status");

        switch (status) {
            case "IN_PROGRESS" -> task.setState(new dz.usthb.eclipseworkspace.task.model.state.InProgressState());
            case "DONE" -> task.setState(new dz.usthb.eclipseworkspace.task.model.state.DoneState());
            case "CANCELLED" -> task.setState(new dz.usthb.eclipseworkspace.task.model.state.CancelledState());
            default -> task.setState(new dz.usthb.eclipseworkspace.task.model.state.TodoState());
        }

        return task;
    }
}
