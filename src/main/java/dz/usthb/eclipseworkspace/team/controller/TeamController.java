package dz.usthb.eclipseworkspace.team.controller;

import java.util.List;

import dz.usthb.eclipseworkspace.team.DTO.TeamMemberView;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;

public class TeamController {

    private final TeamMemberService teamService;

    public TeamController(TeamMemberService teamService) {
        this.teamService = teamService;
        System.out.println("🟨 TeamController initialized with service=" + teamService);
    }

    /* =====================================================
       TEAM MEMBERS – VIEW (USED BY TASK FORM)
    ===================================================== */
    public List<TeamMemberView> getTeamMembersView(Long teamId) {

        System.out.println("🟨 [TeamController] getTeamMembersView ENTER teamId=" + teamId);

        try {
            List<TeamMemberView> result =
                    teamService.getTeamMembersView(teamId);

            System.out.println("🟨 [TeamController] getTeamMembersView RESULT size="
                    + result.size());

            // log each member briefly
            result.forEach(m ->
                    System.out.println("🟨   member → "
                            + m.getFirstName() + " " + m.getLastName()
                            + " | teamMemberId=" + m.getTeamMemberId()
                            + " | role=" + m.getRole()
                            + " | tasks=" + m.getTaskCount())
            );

            return result;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR in getTeamMembersView teamId=" + teamId);
            e.printStackTrace();
            return List.of();
        }
    }

    /* =====================================================
       ADD MEMBER
    ===================================================== */
    public TeamMember addMemberToTeam(AppUser requester, Long teamId, long userId, String role) {

        System.out.println("🟨 [TeamController] addMemberToTeam requester="
                + (requester != null ? requester.getUser_id() : "null")
                + ", teamId=" + teamId
                + ", userId=" + userId
                + ", role=" + role);

        try {
            TeamMember m = teamService.addMember(requester, teamId, userId, role);
            System.out.println("🟨 [TeamController] member added → " + m);
            return m;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR addMemberToTeam");
            e.printStackTrace();
            return null;
        }
    }

    /* =====================================================
       FIND TEAM MEMBER ID BY USER
    ===================================================== */
    public Long findTeamMemberIdByUser(Long teamId, Long userId) {

        System.out.println("🟨 [TeamController] findTeamMemberIdByUser teamId="
                + teamId + ", userId=" + userId);

        try {
            TeamMember member = teamService.getTeamMemberByUser(teamId, userId);

            System.out.println("🟨 [TeamController] found teamMemberId="
                    + member.getTeamMemberId());

            return member.getTeamMemberId();

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR findTeamMemberIdByUser");
            e.printStackTrace();
            return null;
        }
    }

    /* =====================================================
       REMOVE MEMBER
    ===================================================== */
    public boolean removeMemberFromTeam(AppUser requester, Long teamId, Long userId) {

        System.out.println("🟨 [TeamController] removeMemberFromTeam requester="
                + (requester != null ? requester.getUser_id() : "null")
                + ", teamId=" + teamId
                + ", userId=" + userId);

        try {
            Long teamMemberId = findTeamMemberIdByUser(teamId, userId);
            if (teamMemberId == null) {
                System.err.println("🟥 [TeamController] teamMemberId NOT FOUND");
                return false;
            }

            boolean ok = teamService.removeMember(requester, teamMemberId);
            System.out.println("🟨 [TeamController] remove result=" + ok);
            return ok;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR removeMemberFromTeam");
            e.printStackTrace();
            return false;
        }
    }

    /* =====================================================
       UPDATE ROLE
    ===================================================== */
    public boolean updateMemberRole(AppUser requester, Long teamId, Long userId, String newRole) {

        System.out.println("🟨 [TeamController] updateMemberRole requester="
                + (requester != null ? requester.getUser_id() : "null")
                + ", teamId=" + teamId
                + ", userId=" + userId
                + ", newRole=" + newRole);

        try {
            Long teamMemberId = findTeamMemberIdByUser(teamId, userId);
            if (teamMemberId == null) {
                System.err.println("🟥 [TeamController] teamMemberId NOT FOUND");
                return false;
            }

            boolean ok = teamService.updateMemberRole(requester, teamMemberId, newRole);
            System.out.println("🟨 [TeamController] role update result=" + ok);
            return ok;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR updateMemberRole");
            e.printStackTrace();
            return false;
        }
    }

    /* =====================================================
       RAW MEMBERS (NON VIEW)
    ===================================================== */
    public List<TeamMember> getTeamMembers(Long teamId) {

        System.out.println("🟨 [TeamController] getTeamMembers ENTER teamId=" + teamId);

        try {
            List<TeamMember> result = teamService.getTeamMembers(teamId);
            System.out.println("🟨 [TeamController] getTeamMembers RESULT size=" + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR getTeamMembers");
            e.printStackTrace();
            return List.of();
        }
    }

    /* =====================================================
       ASSIGN CHECK
    ===================================================== */
    public boolean canAssignTask(Long teamId, Long userId) {

        System.out.println("🟨 [TeamController] canAssignTask teamId="
                + teamId + ", userId=" + userId);

        try {
            boolean ok = teamService.canAssignTaskToMember(teamId, userId);
            System.out.println("🟨 [TeamController] canAssignTask result=" + ok);
            return ok;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR canAssignTask");
            e.printStackTrace();
            return false;
        }
    }

    /* =====================================================
       OVERLOADED MEMBERS
    ===================================================== */
    public List<TeamMember> getOverloadedMembers(Long teamId) {

        System.out.println("🟨 [TeamController] getOverloadedMembers teamId=" + teamId);

        try {
            List<TeamMember> result = teamService.getOverloadedMembers(teamId);
            System.out.println("🟨 [TeamController] overloaded size=" + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("🟥 [TeamController] ERROR getOverloadedMembers");
            e.printStackTrace();
            return List.of();
        }
    }
}
