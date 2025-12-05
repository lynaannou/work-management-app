package dz.usthb.eclipseworkspace.team.service;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.team.model.PermissionRequest;
import dz.usthb.eclipseworkspace.team.exceptions.PermissionException;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import java.util.List;

public class TeamMemberService extends TeamSubject {
    private final TeamMemberDao teamMemberDao;
    private final PermissionManager permissionManager;
    
    public TeamMemberService(TeamMemberDao teamMemberDao) {
        this.teamMemberDao = teamMemberDao;
        this.permissionManager = new PermissionManager(teamMemberDao);
        
        
        addObserver(new TaskBoardObserver());
        addObserver(new CalendarObserver());
    }
    
    public TeamMemberService(TeamMemberDao teamMemberDao, 
                           PermissionManager permissionManager) {
        this.teamMemberDao = teamMemberDao;
        this.permissionManager = permissionManager;
        
        addObserver(new TaskBoardObserver());
        addObserver(new CalendarObserver());
    }
    
    public TeamMember addMember(AppUser requester, Long teamId, Long userId, String role) 
            throws Exception {
        
        System.out.println("➕ Tentative d'ajout du membre " + userId + " à l'équipe " + teamId);
        
        // Vérification limite de membres (7 max)
        int currentMembers = teamMemberDao.countMembersByTeam(teamId);
        if (currentMembers >= 7) {
            throw new Exception("L'équipe a déjà atteint le maximum de 7 membres");
        }
        
        // Validation des permissions
        PermissionRequest request = new PermissionRequest(requester, teamId, "ADD_MEMBER", userId);
        if (!permissionManager.checkPermission(request)) {
            throw new PermissionException("Permission refusée pour ajouter un membre");
        }
        
        // Vérification si l'utilisateur est déjà membre
        if (teamMemberDao.exists(teamId, userId)) {
            throw new Exception(" L'utilisateur est déjà membre de cette équipe");
        }
        
        // Validation du rôle
        if (!"LEAD".equals(role) && !"MEMBER".equals(role)) {
            throw new Exception(" Rôle invalide. Doit être 'LEAD' ou 'MEMBER'");
        }
        
        // Création et sauvegarde du membre
        TeamMember newMember = new TeamMember(teamId, userId, role);
        Long memberId = teamMemberDao.create(newMember);
        newMember.setTeamMemberId(memberId);
        
        
        notifyMemberAdded(newMember);
        
        System.out.println(" Membre ajouté avec succès: " + newMember);
        return newMember;
    }
    
    public boolean removeMember(AppUser requester, Long teamMemberId) throws Exception {
        TeamMember memberToRemove = teamMemberDao.findById(teamMemberId)
            .orElseThrow(() -> new Exception(" Membre non trouvé"));
        
        System.out.println("➖ Tentative de retrait du membre " + memberToRemove.getUserId());
        
        PermissionRequest request = new PermissionRequest(
            requester, memberToRemove.getTeamId(), "DELETE_MEMBER", memberToRemove.getUserId());
        
        if (!permissionManager.checkPermission(request)) {
            throw new PermissionException(" Permission refusée pour supprimer un membre");
        }
        
        // Empêcher la suppression du dernier LEAD
        if ("LEAD".equals(memberToRemove.getRole())) {
            List<TeamMember> teamMembers = teamMemberDao.findByTeamId(memberToRemove.getTeamId());
            long leadCount = teamMembers.stream()
                .filter(m -> "LEAD".equals(m.getRole()))
                .count();
            if (leadCount <= 1) {
                throw new Exception(" Impossible de supprimer le dernier LEAD de l'équipe");
            }
        }
        
        boolean success = teamMemberDao.delete(teamMemberId);
        if (success) {
            notifyMemberRemoved(memberToRemove);
            System.out.println(" Membre retiré avec succès: " + memberToRemove);
        }
        
        return success;
    }
    
    public boolean updateMemberRole(AppUser requester, Long teamMemberId, String newRole) throws Exception {
        TeamMember member = teamMemberDao.findById(teamMemberId)
            .orElseThrow(() -> new Exception("Membre non trouvé"));
        
        String oldRole = member.getRole();
        
        System.out.println(" Tentative de changement de rôle de " + oldRole + " à " + newRole);
        
        PermissionRequest request = new PermissionRequest(
            requester, member.getTeamId(), "CHANGE_ROLE", member.getUserId());
        request.setNewRole(newRole);
        
        if (!permissionManager.checkPermission(request)) {
            throw new PermissionException(" Permission refusée pour changer le rôle");
        }
        
        if (!"LEAD".equals(newRole) && !"MEMBER".equals(newRole)) {
            throw new Exception(" Rôle invalide. Doit être 'LEAD' ou 'MEMBER'");
        }
        
        boolean success = teamMemberDao.updateRole(teamMemberId, newRole);
        if (success) {
            member.setRole(newRole);
            notifyMemberRoleChanged(member, oldRole);
            System.out.println(" Rôle changé avec succès");
        }
        
        return success;
    }
    
    // Les autres méthodes
    public List<TeamMember> getTeamMembers(Long teamId) throws Exception {
        return teamMemberDao.findByTeamId(teamId);
    }
    
    public boolean isUserMemberOfTeam(Long userId, Long teamId) throws Exception {
        return teamMemberDao.findByTeamAndUser(teamId, userId).isPresent();
    }
    
    public List<TeamMember> getOverloadedMembers(Long teamId) throws Exception {
        return teamMemberDao.findOverloadedMembers(teamId);
    }
    
    public TeamMember getTeamMemberByUser(Long teamId, Long userId) throws Exception {
        return teamMemberDao.findByTeamAndUser(teamId, userId)
            .orElseThrow(() -> new Exception("Utilisateur non membre de cette équipe"));
    }
    
    public boolean canAssignTaskToMember(Long teamId, Long userId) throws Exception {
        TeamMember member = teamMemberDao.findByTeamAndUser(teamId, userId)
            .orElseThrow(() -> new Exception("Membre non trouvé dans l'équipe"));
        
        return member.getTaskCount() < 5;
    }
    
    public int getMemberTaskCount(Long teamMemberId) throws Exception {
        TeamMember member = teamMemberDao.findById(teamMemberId)
            .orElseThrow(() -> new Exception("Membre non trouvé"));
        return member.getTaskCount() != null ? member.getTaskCount() : 0;
    }
}