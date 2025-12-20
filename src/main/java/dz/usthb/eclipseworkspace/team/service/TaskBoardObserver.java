package dz.usthb.eclipseworkspace.team.service;

import java.util.logging.Logger;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

public class TaskBoardObserver implements TeamObserver {
    private static final Logger logger = Logger.getLogger(TaskBoardObserver.class.getName());
    
    @Override
    public void onMemberAdded(TeamMember newMember) {
        logger.info(() -> "TaskBoard: Mise à jour du board pour le nouveau membre " + newMember.getUserId());
        updateTaskAssignments(newMember.getTeamId());
    }
    
    @Override
    public void onMemberRemoved(TeamMember removedMember) {
        logger.info(() -> "TaskBoard: Retrait des tâches assignées au membre " + removedMember.getUserId());
        reassignTasksFromMember(removedMember);
    }
    
    @Override
    public void onMemberRoleChanged(TeamMember member, String oldRole) {
        logger.info(() -> "TaskBoard: Mise à jour des permissions pour le membre " + member.getUserId());
        updateMemberPermissions(member);
    }
    
    private void updateTaskAssignments(Long teamId) {
        logger.info(() -> "Mise à jour des assignations pour l'équipe " + teamId);
    }
    
    private void reassignTasksFromMember(TeamMember removedMember) {
        logger.info(() -> "Réassignation des tâches du membre " + removedMember.getUserId());
    }
    
    private void updateMemberPermissions(TeamMember member) {
        logger.info(() -> "Mise à jour des permissions pour " + member.getUserId());
    }
}