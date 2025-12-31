package dz.usthb.eclipseworkspace.task.model.state;

import dz.usthb.eclipseworkspace.task.model.Task;

public interface TaskState {

    void start(Task task);

    void complete(Task task);

    void cancel(Task task);

    String getName();
}
