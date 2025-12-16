package dz.usthb.eclipseworkspace.workspace.model;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import java.sql.Date;
import java.util.List;

public class Workspace {

    private int team_id;
    private String name;
    private Date createdAt;

    private int openTasksCount;
    private int doneTasksCount;
    private int totalTasksCount;

    private List<TaskProgress> tasks;

    public Workspace(int team_id,
                     String name,
                     Date createdAt,
                     int openTasksCount,
                     int doneTasksCount,
                     int totalTasksCount) {

        this.team_id = team_id;
        this.name = name;
        this.createdAt = createdAt;
        this.openTasksCount = openTasksCount;
        this.doneTasksCount = doneTasksCount;
        this.totalTasksCount = totalTasksCount;
    }

    public int getTeam_id() { return team_id; }
    public String getName() { return name; }
    public Date getCreatedAt() { return createdAt; }
    public int getOpenTasksCount() { return openTasksCount; }
    public int getDoneTasksCount() { return doneTasksCount; }
    public int getTotalTasksCount() { return totalTasksCount; }
    public List<TaskProgress> getTasks() { return tasks; }

    public void setTasks(List<TaskProgress> tasks) {
        this.tasks = tasks;
    }
    public int getTeamId() {
    return team_id;
}

}
