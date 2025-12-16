package dz.usthb.eclipseworkspace.team.model;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;

public class PermissionRequest {
    private AppUser requester;
    private Long teamId;
    private String action; 
    private Long targetUserId;
    private String newRole;
    
    public PermissionRequest(AppUser requester, Long teamId, String action, Long targetUserId) {
        this.requester = requester;
        this.teamId = teamId;
        this.action = action;
        this.targetUserId = targetUserId;
    }
   
    public AppUser getRequester() { return requester; }
    public void setRequester(AppUser requester) { this.requester = requester; }
    
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    
    public String getNewRole() { return newRole; }
    public void setNewRole(String newRole) { this.newRole = newRole; }
}