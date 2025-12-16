package dz.usthb.eclipseworkspace.workspace.service.components;

public class TaskProgress {

    private int todo;
    private int inProgress;
    private int done;
    private int total;
    private int percent;

    public TaskProgress(int todo, int inProgress, int done, int total) {
        this.todo = todo;
        this.inProgress = inProgress;
        this.done = done;
        this.total = total;

        this.percent = (total == 0) ? 0 : (done * 100 / total);
    }

    public int getTodo() { return todo; }
    public int getInProgress() { return inProgress; }
    public int getDone() { return done; }
    public int getTotal() { return total; }
    public int getPercent() { return percent; }

    public void setTodo(int todo) { this.todo = todo; }
    public void setInProgress(int inProgress) { this.inProgress = inProgress; }
    public void setDone(int done) { this.done = done; }
    public void setTotal(int total) { this.total = total; }
}
