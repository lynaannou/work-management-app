package dz.usthb.eclipseworkspace.team.model;

import java.sql.Date;

public final class TeamMember {
    private Long teamMemberId;
    private Long teamId;
    private Long userId;
    private String role; 
    private Date addedAt; 
    private Integer taskCount;
    
    public TeamMember() {}
    
    public TeamMember(Long teamId, Long userId, String role) {
        setTeamId(teamId);
        setUserId(userId);
        setRole(role);
        this.addedAt = new Date(System.currentTimeMillis()); 
    }
    
    public Long getTeamMemberId() { return teamMemberId; }
    public void setTeamMemberId(Long teamMemberId) { 
        if (teamMemberId != null && teamMemberId <= 0) {
            throw new IllegalArgumentException("ID de membre d'équipe invalide");
        }
        this.teamMemberId = teamMemberId; 
    }
    
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { 
        if (teamId == null || teamId <= 0) {
            throw new IllegalArgumentException("ID d'équipe invalide");
        }
        this.teamId = teamId; 
    }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { 
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("ID utilisateur invalide");
        }
        this.userId = userId; 
    }
    
    public String getRole() { return role; }
    public void setRole(String role) { 
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Le rôle ne peut pas être vide");
        }
        // CETTE VALIDATION EST CRITIQUE
        if (!"LEAD".equals(role) && !"MEMBER".equals(role)) {
            throw new IllegalArgumentException("Rôle invalide. Doit être 'LEAD' ou 'MEMBER'");
        }
        this.role = role; 
    }
    public Date getAddedAt() { return addedAt; }
    public Integer getTaskCount() { return taskCount; }
    
    @Override
    public String toString() {
        return String.format(
            "TeamMember{id=%d, teamId=%d, userId=%d, role='%s', tasks=%d}",
            teamMemberId, teamId, userId, role, taskCount
        );
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public void setAddedAt(Date date) {
        this.addedAt = date;
    }
    

}