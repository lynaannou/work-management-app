package dz.usthb.eclipseworkspace.todo.controller;

import dz.usthb.eclipseworkspace.todo.dao.DaoTodoTask;
import dz.usthb.eclipseworkspace.todo.model.TodoTask;
import dz.usthb.eclipseworkspace.todo.service.TodoService;
import dz.usthb.eclipseworkspace.todo.strategy.SimpleProgressStrategy;
import dz.usthb.eclipseworkspace.user.util.Session;

import java.util.List;

public class TodoController {

    private final TodoService service;

    public TodoController(DaoTodoTask daoTodoTask) {
        this.service = new TodoService(
                daoTodoTask,
                new SimpleProgressStrategy()
        );
    }

    public String loadTodosJson(Long userId) throws Exception {
        List<TodoTask> tasks = service.getTodos(userId);
        int progress = service.getProgress(tasks);
        return TodoJsonSerializer.toJson(tasks, progress);
    }

    public void changeStatus(int taskId, String status) throws Exception {
        service.updateStatus(taskId, status);
    }
    public void addTodo(String title, String description, String dueDate, String status) {
    try {
        Long userId = Session.getInstance().getUserId();
        service.addItem(
            userId,
            title,
            description,
            dueDate,
            status
        );
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
    public void deleteTodo(int itemId) throws Exception {
    service.deleteItem(itemId);
    }


}
