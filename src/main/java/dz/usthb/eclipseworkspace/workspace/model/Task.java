package dz.usthb.eclipseworkspace.workspace.model;

import java.sql.Date;

public class Task {

    private int task_id;
    private int team_id;
    private Integer teamMemberId;
    private String title;
    private String description;
    private String status;
    private Date startDate;
    private Date endDate;
    private int progressPct;
    private Date createdAt;
    private Date completedAt;

    public Task() {}

    public Task(int task_id, int team_id, Integer teamMemberId, String title,
                String description, String status, Date startDate, Date endDate,
                int progressPct, Date createdAt, Date completedAt) {

        this.task_id = task_id;
        this.team_id = team_id;
        this.teamMemberId = teamMemberId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progressPct = progressPct;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

 
    public int getTask_id() { return task_id; }
    public int getTeam_id() { return team_id; }
    public Integer getTeamMemberId() { return teamMemberId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public int getProgressPct() { return progressPct; }
    public Date getCreatedAt() { return createdAt; }
    public Date getCompletedAt() { return completedAt; }
    
    public void setProgressPct(int progressPct) {
        this.progressPct = progressPct;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public void setTeamMemberId(Integer teamMemberId) {
        this.teamMemberId = teamMemberId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }
    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
}
