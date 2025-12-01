package dz.usthb.eclipseworkspace.workspace.service.components;

import dz.usthb.eclipseworkspace.workspace.model.Task;

public class TaskComponent implements WorkspaceComponent {

    private final Task task;

    public TaskComponent(Task task) {
        this.task = task;
    }

    @Override
    public void display() {
        System.out.println(
            "Task: " + task.getTitle() +
            " | Progress: " + task.getProgressPct() + "%" +
            " | Status: " + task.getStatus()
        );
    }

    @Override
    public int getProgress() {
        return task.getProgressPct();
    }

    public String getStatus() {
        return task.getStatus();
    }
}
