package dz.usthb.eclipseworkspace.team.DTO;

public class TeamMemberView {

    private Long teamMemberId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String role;
    private Integer taskCount;

    public Long getTeamMemberId() { return teamMemberId; }
    public void setTeamMemberId(Long teamMemberId) { this.teamMemberId = teamMemberId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getTaskCount() { return taskCount; }
    public void setTaskCount(Integer taskCount) { this.taskCount = taskCount; }
    public class WorkspaceView {
    private Long teamId;
    private String name;
    private String description; // ✅ MANQUANT
}

}
