package dz.usthb.eclipseworkspace.todo.strategy;

import dz.usthb.eclipseworkspace.todo.model.TodoTask;
import java.util.List;

public class SimpleProgressStrategy implements ProgressStrategy {

    @Override
    public int calculate(List<TodoTask> tasks) {
        if (tasks.isEmpty()) return 0;

        long done = tasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .count();

        return (int) ((done * 100) / tasks.size());
    }
}
