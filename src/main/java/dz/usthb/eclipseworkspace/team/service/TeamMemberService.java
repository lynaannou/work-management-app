package dz.usthb.eclipseworkspace.team.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import dz.usthb.eclipseworkspace.team.DTO.TeamMemberView;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.exceptions.PermissionException;
import dz.usthb.eclipseworkspace.team.model.PermissionRequest;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.user.util.UserRole;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;

public class TeamMemberService extends TeamSubject {
    private final TeamMemberDao teamMemberDao;
    private final PermissionManager permissionManager;
    private static final int MAX_TEAM_MEMBERS = 7;
    private static final int MAX_TASKS_PER_MEMBER = 5;
    
    public TeamMemberService(TeamMemberDao teamMemberDao) {
        if (teamMemberDao == null) {
            throw new IllegalArgumentException("TeamMemberDao ne peut pas être null");
        }
        this.teamMemberDao = teamMemberDao;
        this.permissionManager = new PermissionManager(teamMemberDao);
        
        addObserver(new TaskBoardObserver());
        addObserver(new CalendarObserver());
    }
    
    public TeamMemberService(TeamMemberDao teamMemberDao, 
                           PermissionManager permissionManager) {
        if (teamMemberDao == null) {
            throw new IllegalArgumentException("TeamMemberDao ne peut pas être null");
        }
        if (permissionManager == null) {
            throw new IllegalArgumentException("PermissionManager ne peut pas être null");
        }
        this.teamMemberDao = teamMemberDao;
        this.permissionManager = permissionManager;
        
        addObserver(new TaskBoardObserver());
        addObserver(new CalendarObserver());
    }
    

    public TeamMember createTeamWithLeader(Long teamId, AppUser creator) throws Exception {
        validateTeamId(teamId);
        validateAppUser(creator, "créateur");
        
        System.out.println(" Création d'équipe avec leader: " + creator.getUser_id());
        
        if (teamMemberDao.exists(teamId, Long.valueOf(creator.getUser_id()))) {
            throw new Exception("L'utilisateur est déjà membre de cette équipe");
        }
        
        TeamMember leader = new TeamMember(teamId, Long.valueOf(creator.getUser_id()), "LEAD");
        Long memberId = teamMemberDao.create(leader);
        leader.setTeamMemberId(memberId);
        
        notifyMemberAdded(leader);
        System.out.println(" Équipe créée avec le leader: " + leader);
        return leader;
    }
    
    public TeamMember addMember(AppUser requester, Long teamId, Long userId, String role) 
            throws Exception {
        
        validateAddMemberParameters(requester, teamId, userId, role);
        
        System.out.println(" Tentative d'ajout du membre " + userId + " à l'équipe " + teamId);

        int currentMembers = teamMemberDao.countMembersByTeam(teamId);
        if (currentMembers >= MAX_TEAM_MEMBERS) {
            throw new Exception("L'équipe a déjà atteint le maximum de " + MAX_TEAM_MEMBERS + " membres");
        }
        
        PermissionRequest request = new PermissionRequest(requester, teamId, "ADD_MEMBER", userId);
        if (!permissionManager.checkPermission(request)) {
            throw new PermissionException("Permission refusée pour ajouter un membre");
        }

        if (teamMemberDao.exists(teamId, userId)) {
            throw new Exception("L'utilisateur est déjà membre de cette équipe");
        }
        
        TeamMember newMember = new TeamMember(teamId, userId, role);
        Long memberId = teamMemberDao.create(newMember);
        newMember.setTeamMemberId(memberId);
        
        notifyMemberAdded(newMember);
        
        System.out.println(" Membre ajouté avec succès: " + newMember);
        return newMember;
    }
  public List<TeamMemberView> getTeamMembersView(Long teamId) throws SQLException {

    System.out.println("🟩 [TeamMemberService] getTeamMembersView ENTER");
    System.out.println("🟩 [TeamMemberService] teamId = " + teamId);

    if (teamId == null) {
        System.err.println("❌ [TeamMemberService] teamId is NULL");
        return List.of();
    }

    if (teamId <= 0) {
        System.err.println("❌ [TeamMemberService] teamId is INVALID: " + teamId);
        return List.of();
    }

    System.out.println("🟩 [TeamMemberService] calling DAO.findTeamMembersView...");

    List<TeamMemberView> views = teamMemberDao.findTeamMembersView(teamId);

    System.out.println("🟩 [TeamMemberService] DAO returned " + views.size() + " members");

    if (views.isEmpty()) {
        System.out.println("⚠️ [TeamMemberService] NO TEAM MEMBERS FOUND");
    } else {
        for (TeamMemberView v : views) {
            System.out.println(
                "🟩 [TeamMemberService] MEMBER → "
                + "teamMemberId=" + v.getTeamMemberId()
                + ", userId=" + v.getUserId()
                + ", name=" + v.getFirstName() + " " + v.getLastName()
                + ", role=" + v.getRole()
                + ", taskCount=" + v.getTaskCount()
            );
        }
    }

    System.out.println("🟩 [TeamMemberService] getTeamMembersView EXIT");

    return views;
}


    
    public boolean removeMember(AppUser requester, Long teamMemberId) throws Exception {
        validateAppUser(requester, "demandeur");
        validateTeamMemberId(teamMemberId);
        
        TeamMember memberToRemove = teamMemberDao.findById(teamMemberId)
            .orElseThrow(() -> new Exception("Membre non trouvé avec l'ID: " + teamMemberId));
        
        System.out.println("Tentative de retrait du membre " + memberToRemove.getUserId());
        
        PermissionRequest request = new PermissionRequest(
            requester, memberToRemove.getTeamId(), "DELETE_MEMBER", memberToRemove.getUserId());
        
        if (!permissionManager.checkPermission(request)) {
            throw new PermissionException("Permission refusée pour supprimer un membre");
        }

        if ("LEAD".equals(memberToRemove.getRole())) {
            List<TeamMember> teamMembers = teamMemberDao.findByTeamId(memberToRemove.getTeamId());
            long leadCount = teamMembers.stream()
                .filter(m -> "LEAD".equals(m.getRole()))
                .count();
            if (leadCount <= 1) {
                throw new Exception("Impossible de supprimer le dernier LEAD de l'équipe");
            }
        }
        
        boolean success = teamMemberDao.delete(teamMemberId);
        if (success) {
            notifyMemberRemoved(memberToRemove);
            System.out.println(" Membre retiré avec succès: " + memberToRemove);
        } else {
            System.out.println(" Échec de la suppression du membre");
        }
        
        return success;
    }
    
    public boolean updateMemberRole(AppUser requester, Long teamMemberId, String newRole) throws Exception {
        validateAppUser(requester, "demandeur");
        validateTeamMemberId(teamMemberId);
        validateRole(newRole);
        
        TeamMember member = teamMemberDao.findById(teamMemberId)
            .orElseThrow(() -> new Exception("Membre non trouvé avec l'ID: " + teamMemberId));
        
        String oldRole = member.getRole();

        if (oldRole.equals(newRole)) {
            System.out.println("Le rôle est déjà " + newRole + ". Aucun changement nécessaire.");
            return true;
        }
        
        System.out.println(" Tentative de changement de rôle de " + oldRole + " à " + newRole);
        
        PermissionRequest request = new PermissionRequest(
            requester, member.getTeamId(), "CHANGE_ROLE", member.getUserId());
        request.setNewRole(newRole);
        
        if (!permissionManager.checkPermission(request)) {
            throw new PermissionException("Permission refusée pour changer le rôle");
        }
        
        boolean success = teamMemberDao.updateRole(teamMemberId, newRole);
        if (success) {
            member.setRole(newRole);
            notifyMemberRoleChanged(member, oldRole);
            System.out.println(" Rôle changé avec succès");
        } else {
            System.out.println(" Échec du changement de rôle");
        }
        
        return success;
    }
    

    public List<TeamMember> getTeamMembers(Long teamId) throws Exception {
        validateTeamId(teamId);
        return teamMemberDao.findByTeamId(teamId);
    }
    
    public boolean isUserMemberOfTeam(Long userId, Long teamId) throws Exception {
        validateUserId(userId);
        validateTeamId(teamId);
        return teamMemberDao.findByTeamAndUser(teamId, userId).isPresent();
    }
    
    public List<TeamMember> getOverloadedMembers(Long teamId) throws Exception {
        validateTeamId(teamId);
        return teamMemberDao.findOverloadedMembers(teamId);
    }
    
    public TeamMember getTeamMemberByUser(Long teamId, Long userId) throws Exception {
        validateTeamId(teamId);
        validateUserId(userId);
        return teamMemberDao.findByTeamAndUser(teamId, userId)
            .orElseThrow(() -> new Exception("Utilisateur non membre de cette équipe"));
    }
    
    public boolean canAssignTaskToMember(Long teamId, Long userId) throws Exception {
        validateTeamId(teamId);
        validateUserId(userId);
        
        TeamMember member = teamMemberDao.findByTeamAndUser(teamId, userId)
            .orElseThrow(() -> new Exception("Membre non trouvé dans l'équipe"));
        
        return member.getTaskCount() == null || member.getTaskCount() < MAX_TASKS_PER_MEMBER;
    }
    
    public int getMemberTaskCount(Long teamMemberId) throws Exception {
        validateTeamMemberId(teamMemberId);
        
        TeamMember member = teamMemberDao.findById(teamMemberId)
            .orElseThrow(() -> new Exception("Membre non trouvé"));
      return Optional.ofNullable(member.getTaskCount()).orElse(0);
    }
    
    private void validateTeamId(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new IllegalArgumentException("ID d'équipe invalide");
        }
    }
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("ID utilisateur invalide");
        }
    }
    
    private void validateTeamMemberId(Long teamMemberId) {
        if (teamMemberId == null || teamMemberId <= 0) {
            throw new IllegalArgumentException("ID de membre d'équipe invalide");
        }
    }
    
    private void validateAppUser(AppUser user, String context) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur (" + context + ") ne peut pas être null");
        }
        if (user.getUser_id() <= 0) {
            throw new IllegalArgumentException("L'ID utilisateur (" + context + ") doit être positif");
        }
    }
    
    private void validateRole(String role) {
        if (role == null || (!"LEAD".equals(role) && !"MEMBER".equals(role))) {
            throw new IllegalArgumentException("Rôle invalide. Doit être 'LEAD' ou 'MEMBER'");
        }
    }

    public UserRole getUserRole(Long userId) throws Exception {
        // For simplicity, just get the first team membership role, default to MEMBER
        List<TeamMember> memberships = teamMemberDao.findByUserId(userId);
        if (memberships.isEmpty()) {
            return UserRole.MEMBER;
        }
        // Assume first membership is main role (adjust if needed)
        String roleStr = memberships.get(0).getRole();
        return roleStr != null ? UserRole.valueOf(roleStr.toUpperCase()) : UserRole.MEMBER;
    }
    
    private void validateAddMemberParameters(AppUser requester, Long teamId, Long userId, String role) {
        validateAppUser(requester, "demandeur");
        validateTeamId(teamId);
        validateUserId(userId);
        validateRole(role);
    }
}