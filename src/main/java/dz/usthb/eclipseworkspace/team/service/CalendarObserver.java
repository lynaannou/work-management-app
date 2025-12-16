package dz.usthb.eclipseworkspace.team.service;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

public class CalendarObserver implements TeamObserver {
    
    @Override
    public void onMemberAdded(TeamMember newMember) {
        System.out.println("📅 Calendar: Ajout du membre " + newMember.getUserId() + " au calendrier d'équipe");
        syncTeamCalendar(newMember);
    }
    
    @Override
    public void onMemberRemoved(TeamMember removedMember) {
        System.out.println("📅 Calendar: Retrait du membre " + removedMember.getUserId() + " du calendrier d'équipe");
        unsyncTeamCalendar(removedMember);
    }
    
    @Override
    public void onMemberRoleChanged(TeamMember member, String oldRole) {
        System.out.println("📅 Calendar: Mise à jour des droits calendrier pour " + member.getUserId());
        updateCalendarPermissions(member);
    }
    
    private void syncTeamCalendar(TeamMember member) {
        System.out.println("Synchronisation calendrier pour " + member.getUserId());
    }
    
    private void unsyncTeamCalendar(TeamMember member) {
        System.out.println("Désynchronisation calendrier pour " + member.getUserId());
    }
    
    private void updateCalendarPermissions(TeamMember member) {
        System.out.println("Mise à jour permissions calendrier pour " + member.getUserId());
    }
}