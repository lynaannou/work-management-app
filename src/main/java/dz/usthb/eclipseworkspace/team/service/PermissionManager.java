package dz.usthb.eclipseworkspace.team.service;

import java.util.Optional;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.exceptions.PermissionException;
import dz.usthb.eclipseworkspace.team.model.PermissionRequest;
import dz.usthb.eclipseworkspace.team.model.TeamMember;

public class PermissionManager {
    private final TeamMemberDao teamMemberDao;
    
    public PermissionManager(TeamMemberDao teamMemberDao) {
        this.teamMemberDao = teamMemberDao;
    }
    
    public boolean checkPermission(PermissionRequest request) throws PermissionException {
        if ("DELETE_MEMBER".equals(request.getAction()) && 
            request.getRequester().getUser_id() == request.getTargetUserId()) {
            throw new PermissionException("Un utilisateur ne peut pas se retirer lui-même de l'équipe");
        }
        
        return checkLeadPermissions(request);
    }
    
    private boolean checkLeadPermissions(PermissionRequest request) throws PermissionException {
        try {
            Optional<TeamMember> requesterMember = teamMemberDao.findByTeamAndUser(
                request.getTeamId(), Long.valueOf(request.getRequester().getUser_id()));
                
            boolean isLead = requesterMember.isPresent() && "LEAD".equals(requesterMember.get().getRole());
            
            if (isLead) {
                System.out.println("PermissionManager: LEAD autorisé pour " + request.getAction());
                return true;
            }
            
            switch (request.getAction()) {
                case "ADD_MEMBER":
                    throw new PermissionException("Seul le LEAD peut ajouter des membres à l'équipe");
                    
                case "CHANGE_ROLE":
                    // Vérifier si c'est l'utilisateur lui-même
                    if (request.getRequester().getUser_id() == request.getTargetUserId()) {
                        // Un membre peut se rétrograder lui-même en MEMBER
                        if ("MEMBER".equals(request.getNewRole())) {
                            return true;
                        }
                        throw new PermissionException("Un membre ne peut pas se promouvoir lui-même en LEAD");
                    }
                    throw new PermissionException("Seul le LEAD peut changer le rôle d'un autre membre");
                    
                case "DELETE_MEMBER":
                    throw new PermissionException("Seul le LEAD peut supprimer des membres");
                    
                default:
                    throw new PermissionException("Action non autorisée: " + request.getAction());
            }
            
        } catch (Exception e) {
            if (e instanceof PermissionException) {
                throw (PermissionException) e;
            }
            throw new PermissionException("Erreur de vérification des permissions: " + e.getMessage(), e);
        }
    }
}