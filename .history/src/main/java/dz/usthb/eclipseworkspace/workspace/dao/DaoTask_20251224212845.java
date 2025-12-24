package dz.usthb.eclipseworkspace.workspace.dao;

import dz.usthb.eclipseworkspace.workspace.model.Task;
import dz.usthb.eclipseworkspace.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoTask {

    // ==========================
    // FIND BY ID
    // ==========================
    public Task findById(int id) throws SQLException {

        String sql = "SELECT * FROM task WHERE task_id = ?";

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
    // FIND TASKS BY TEAM
    // ==========================
    public List<Task> findByTeam(int teamId) throws SQLException {

        System.out.println("🗂️ [DaoTask] findByTeam teamId = " + teamId);

        String sql = "SELECT * FROM task WHERE team_id = ? ORDER BY task_id";
        List<Task> list = new ArrayList<>();

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task t = map(rs);
                    list.add(t);

                    System.out.println(
                        "   ➜ Task loaded: id=" + t.getTask_id()
                        + " title=" + t.getTitle()
                    );
                }
            }
        }

        System.out.println("📊 Total tasks loaded = " + list.size());
        return list;
    }

    // ==========================
    // INSERT TASK
    // ==========================
    public void insert(Task t) throws SQLException {

        if (t.getTitle() == null || t.getTitle().isBlank()) {
            throw new SQLException("❌ Task title cannot be NULL or empty");
        }

        String sql = """
            INSERT INTO task (
                team_id,
                team_member_id,
                title,
                description,
                status,
                start_date,
                due_date,
                progress_pct
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, t.getTeam_id());
            ps.setObject(2, t.getTeamMemberId());
            ps.setString(3, t.getTitle());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getStatus());
            ps.setDate(6, t.getStartDate());
            ps.setDate(7, t.getEndDate());
            ps.setInt(8, t.getProgressPct());

            ps.executeUpdate();
            System.out.println("✅ Task inserted: " + t.getTitle());
        }
    }

    // ==========================
    // UPDATE STATUS
    // ==========================
    public void updateStatus(int taskId, String status) throws SQLException {

        String sql = "UPDATE task SET status = ? WHERE task_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.executeUpdate();

            System.out.println("🔄 Task " + taskId + " status updated to " + status);
        }
    }

    // ==========================
    // ROW MAPPER
    // ==========================
    private Task map(ResultSet rs) throws SQLException {
        return new Task(
            rs.getInt("task_id"),
            rs.getInt("team_id"),
            (Integer) rs.getObject("team_member_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("status"),
            rs.getDate("start_date"),
            rs.getDate("due_date"),
            rs.getInt("progress_pct"),
            rs.getDate("created_at"),
            rs.getDate("completed_at")
        );
    }
}
