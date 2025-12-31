package dz.usthb.eclipseworkspace.todo.service;

import dz.usthb.eclipseworkspace.todo.dao.DaoTodoTask;
import dz.usthb.eclipseworkspace.todo.model.TodoTask;
import dz.usthb.eclipseworkspace.todo.observer.TaskObserver;
import dz.usthb.eclipseworkspace.todo.observer.TaskSubject;
import dz.usthb.eclipseworkspace.todo.strategy.ProgressStrategy;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

public class TodoService implements TaskSubject {

    private final DaoTodoTask dao;
    private final ProgressStrategy strategy;
    private final List<TaskObserver> observers = new ArrayList<>();

    public TodoService(DaoTodoTask dao, ProgressStrategy strategy) {
        this.dao = dao;
        this.strategy = strategy;
    }

    public List<TodoTask> getTodos(Long userId) throws Exception {
        return dao.findByUser(userId);
    }

    public int getProgress(List<TodoTask> tasks) {
        return strategy.calculate(tasks);
    }

    public void updateStatus(int taskId, String status) throws Exception {
        dao.updateStatus(taskId, status);
        notifyObservers();
    }
    public void deleteItem(int itemId) throws Exception {
    dao.deleteItem(itemId);
    notifyObservers();
    }

    public void addItem(
        Long userId,
        String title,
        String description,
        String dueDate,
        String status
) throws Exception {

    dao.insertTodoItem(
        userId,
        title,
        description,
        Date.valueOf(dueDate),
        status
    );

    notifyObservers();
}


    @Override
    public void addObserver(TaskObserver o) {
        observers.add(o);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(TaskObserver::onTaskUpdated);
    }
}
