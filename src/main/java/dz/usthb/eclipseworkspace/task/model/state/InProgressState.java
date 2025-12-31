package dz.usthb.eclipseworkspace.task.model.state;

import dz.usthb.eclipseworkspace.task.model.Task;

public class InProgressState implements TaskState {

    @Override
    public void start(Task task) {
        throw new IllegalStateException("La tâche est déjà en cours.");
    }

    @Override
    public void complete(Task task) {
        task.setProgressPct(100);
        task.setState(new DoneState());
    }

    @Override
    public void cancel(Task task) {
        task.setState(new CancelledState());
    }

    @Override
    public String getName() {
        return "IN_PROGRESS";
    }
}
