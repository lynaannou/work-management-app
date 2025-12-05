package dz.usthb.eclipseworkspace.team.model;

import java.sql.Date;

public class TeamMember {
    private Long teamMemberId;
    private Long teamId;
    private Long userId;
    private String role; 
    private Date addedAt; 
    private Integer taskCount;
    
    public TeamMember() {}
    
    public TeamMember(Long teamId, Long userId, String role) {
        this.teamId = teamId;
        this.userId = userId;
        this.role = role;
        this.addedAt = new Date(System.currentTimeMillis()); 
    }
    
    public Long getTeamMemberId() { return teamMemberId; }
    public void setTeamMemberId(Long teamMemberId) { this.teamMemberId = teamMemberId; }
    
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Date getAddedAt() { return addedAt; }
    public void setAddedAt(Date addedAt) { this.addedAt = addedAt; }
    
    public Integer getTaskCount() { return taskCount; }
    public void setTaskCount(Integer taskCount) { this.taskCount = taskCount; }
    
    @Override
    public String toString() {
        return String.format(
            "TeamMember{id=%d, teamId=%d, userId=%d, role='%s', tasks=%d}",
            teamMemberId, teamId, userId, role, taskCount
        );
    }
}