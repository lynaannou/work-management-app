package dz.usthb.eclipseworkspace.team.service;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

public class TaskBoardObserver implements TeamObserver {
    
    @Override
    public void onMemberAdded(TeamMember newMember) {
        System.out.println(" TaskBoard: Mise à jour du board pour le nouveau membre " + newMember.getUserId());
        updateTaskAssignments(newMember.getTeamId());
    }
    
    @Override
    public void onMemberRemoved(TeamMember removedMember) {
        System.out.println(" TaskBoard: Retrait des tâches assignées au membre " + removedMember.getUserId());
        reassignTasksFromMember(removedMember);
    }
    
    @Override
    public void onMemberRoleChanged(TeamMember member, String oldRole) {
        System.out.println(" TaskBoard: Mise à jour des permissions pour le membre " + member.getUserId());
        updateMemberPermissions(member);
    }
    
    private void updateTaskAssignments(Long teamId) {
        System.out.println("Mise à jour des assignations pour l'équipe " + teamId);
    }
    
    private void reassignTasksFromMember(TeamMember removedMember) {
        System.out.println("Réassignation des tâches du membre " + removedMember.getUserId());
    }
    
    private void updateMemberPermissions(TeamMember member) {
        System.out.println("Mise à jour des permissions pour " + member.getUserId());
    }
}