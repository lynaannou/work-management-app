package dz.usthb.eclipseworkspace.task.model.state;

import dz.usthb.eclipseworkspace.task.model.Task;

public class TodoState implements TaskState {

    @Override
    public void start(Task task) {
        task.setState(new InProgressState());
    }

    @Override
    public void complete(Task task) {
        throw new IllegalStateException(
            "Impossible de terminer une tâche qui n'a pas commencé."
        );
    }

    @Override
    public void cancel(Task task) {
        task.setState(new CancelledState());
    }

    @Override
    public String getName() {
        return "TODO";
    }
}
