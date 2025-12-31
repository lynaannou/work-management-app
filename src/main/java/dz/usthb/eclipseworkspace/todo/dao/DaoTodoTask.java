package dz.usthb.eclipseworkspace.todo.dao;

import dz.usthb.eclipseworkspace.todo.model.TodoTask;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoTodoTask {

    private final Connection connection;

    public DaoTodoTask(Connection connection) {
        this.connection = connection;
    }

    public List<TodoTask> findByUser(Long userId) throws SQLException {

    String sql = """
        SELECT
            ti.item_id,
            ti.title,
            ti.description,
            ti.due_date,
            ti.status
        FROM todo_item ti
        JOIN todo t ON t.todo_id = ti.todo_id
        WHERE t.user_id = ?
        ORDER BY ti.due_date
    """;

    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setLong(1, userId);

    ResultSet rs = ps.executeQuery();
    List<TodoTask> list = new ArrayList<>();

    while (rs.next()) {
        list.add(new TodoTask(
            rs.getInt("item_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getDate("due_date"),
            rs.getString("status")
        ));
    }

    return list;
 }


    public void updateStatus(int itemId, String status) throws SQLException {
    String sql = "UPDATE todo_item SET status = ? WHERE item_id = ?";
    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setString(1, status);
    ps.setInt(2, itemId);
    ps.executeUpdate();
    }

    public void insertTodoItem(
        Long userId,
        String title,
        String description,
        Date dueDate,
        String status
) throws SQLException {

    String sql = """
        INSERT INTO todo_item (todo_id, title, description, due_date, status)
        VALUES (
            (SELECT todo_id FROM todo WHERE user_id = ?),
            ?, ?, ?, ?
        )
    """;

    PreparedStatement ps = connection.prepareStatement(sql);
    ps.setLong(1, userId);
    ps.setString(2, title);
    ps.setString(3, description);
    ps.setDate(4, dueDate);
    ps.setString(5, status);

    ps.executeUpdate();
}

}
