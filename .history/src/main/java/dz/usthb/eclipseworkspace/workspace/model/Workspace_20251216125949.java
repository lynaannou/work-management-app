package dz.usthb.eclipseworkspace.workspace.model;

import java.sql.Date;

public class Workspace {

    // ✅ ONE field, ONE name
    private int teamId;
    private String name;
    private Date createdAt;

    private int openTasksCount;
    private int doneTasksCount;
    private int totalTasksCount;

    public Workspace(int teamId,
                     String name,
                     Date createdAt,
                     int openTasksCount,
                     int doneTasksCount,
                     int totalTasksCount) {

        this.teamId = teamId;
        this.name = name;
        this.createdAt = createdAt;
        this.openTasksCount = openTasksCount;
        this.doneTasksCount = doneTasksCount;
        this.totalTasksCount = totalTasksCount;
    }

    // ✅ ONLY camelCase getters
    public int getTeamId() { return teamId; }
    public String getName() { return name; }
    public Date getCreatedAt() { return createdAt; }
    public int getOpenTasksCount() { return openTasksCount; }
    public int getDoneTasksCount() { return doneTasksCount; }
    public int getTotalTasksCount() { return totalTasksCount; }
    // ✅ ONLY camelCase setters
    public void setTeamId(int teamId) { this.teamId = teamId; } 
    public void setName(String name) { this.name = name; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setOpenTasksCount(int openTasksCount) { this.openTasksCount = openTasksCount; }
    public void setDoneTasksCount(int doneTasksCount) { this.doneTasksCount = doneTasksCount; }
    public void setTotalTasksCount(int totalTasksCount) { this.totalTasksCount = totalTasksCount; }
    

}
