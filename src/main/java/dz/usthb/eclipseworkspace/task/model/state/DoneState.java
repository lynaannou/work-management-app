package dz.usthb.eclipseworkspace.task.model.state;

import dz.usthb.eclipseworkspace.task.model.Task;

public class DoneState implements TaskState {

    @Override
    public void start(Task task) {
        throw new IllegalStateException("La tâche est déjà terminée.");
    }

    @Override
    public void complete(Task task) {
        throw new IllegalStateException("La tâche est déjà terminée.");
    }

    @Override
    public void cancel(Task task) {
        throw new IllegalStateException("Impossible d'annuler une tâche terminée.");
    }

    @Override
    public String getName() {
        return "DONE";
    }
}
