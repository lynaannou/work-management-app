package dz.usthb.eclipseworkspace.todo.strategy;

import dz.usthb.eclipseworkspace.todo.model.TodoTask;
import java.util.List;

public interface ProgressStrategy {
    int calculate(List<TodoTask> tasks);
}
