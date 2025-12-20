package dz.usthb.eclipseworkspace.team.controller;

import java.util.List;

import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;

public class TeamController {
    private final TeamMemberService teamService;
    
    public TeamController(TeamMemberService teamService) {
        this.teamService = teamService;
    }
    
    public TeamMember addMemberToTeam(AppUser requester, Long teamId, long userId, String role) {
        try {
            return teamService.addMember(requester, teamId, userId, role);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du membre: " + e.getMessage());
            return null;
        }
    }
    
    // Ajouter cette méthode pour obtenir le teamMemberId par utilisateur
    public Long findTeamMemberIdByUser(Long teamId, Long userId) {
        try {
            TeamMember member = teamService.getTeamMemberByUser(teamId, userId);
            return member.getTeamMemberId();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du membre: " + e.getMessage());
            return null;
        }
    }
    
    public boolean removeMemberFromTeam(AppUser requester, Long teamId, Long userId) {
        try {
            // Trouver d'abord le teamMemberId
            Long teamMemberId = findTeamMemberIdByUser(teamId, userId);
            if (teamMemberId == null) {
                System.err.println("Membre non trouvé pour l'utilisateur: " + userId);
                return false;
            }
            
            return teamService.removeMember(requester, teamMemberId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du membre: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateMemberRole(AppUser requester, Long teamId, Long userId, String newRole) {
        try {
            // Trouver d'abord le teamMemberId
            Long teamMemberId = findTeamMemberIdByUser(teamId, userId);
            if (teamMemberId == null) {
                System.err.println("Membre non trouvé pour l'utilisateur: " + userId);
                return false;
            }
            
            return teamService.updateMemberRole(requester, teamMemberId, newRole);
        } catch (Exception e) {
            System.err.println("Erreur lors du changement de rôle: " + e.getMessage());
            return false;
        }
    }
    
    public List<TeamMember> getTeamMembers(Long teamId) {
        try {
            return teamService.getTeamMembers(teamId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des membres: " + e.getMessage());
            return List.of();
        }
    }
    
    public boolean canAssignTask(Long teamId, Long userId) {
        try {
            return teamService.canAssignTaskToMember(teamId, userId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
            return false;
        }
    }
    
    public List<TeamMember> getOverloadedMembers(Long teamId) {
        try {
            return teamService.getOverloadedMembers(teamId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des membres surchargés: " + e.getMessage());
            return List.of();
        }
    }
}