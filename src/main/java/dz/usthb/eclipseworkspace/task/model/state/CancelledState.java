package dz.usthb.eclipseworkspace.task.model.state;

import dz.usthb.eclipseworkspace.task.model.Task;

public class CancelledState implements TaskState {

    @Override
    public void start(Task task) {
        throw new IllegalStateException("Impossible de redémarrer une tâche annulée.");
    }

    @Override
    public void complete(Task task) {
        throw new IllegalStateException("Impossible de terminer une tâche annulée.");
    }

    @Override
    public void cancel(Task task) {
        throw new IllegalStateException("La tâche est déjà annulée.");
    }

    @Override
    public String getName() {
        return "CANCELLED";
    }
}
