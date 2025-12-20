package dz.usthb.eclipseworkspace.team.service;

import java.util.logging.Logger;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

public class CalendarObserver implements TeamObserver {
    private static final Logger logger = Logger.getLogger(CalendarObserver.class.getName());
    
    @Override
    public void onMemberAdded(TeamMember newMember) {
        logger.info("Calendar: Ajout du membre " + newMember.getUserId() + " au calendrier d'équipe");
        syncTeamCalendar(newMember);
    }
    
    @Override
    public void onMemberRemoved(TeamMember removedMember) {
        logger.info("Calendar: Retrait du membre " + removedMember.getUserId() + " du calendrier d'équipe");
        unsyncTeamCalendar(removedMember);
    }
    
    @Override
    public void onMemberRoleChanged(TeamMember member, String oldRole) {
        logger.info("Calendar: Mise à jour des droits calendrier pour " + member.getUserId());
        updateCalendarPermissions(member);
    }
    
    private void syncTeamCalendar(TeamMember member) {
        logger.info("Synchronisation calendrier pour " + member.getUserId());
        // TODO: Implémenter la synchronisation avec le calendrier
    }
    
    private void unsyncTeamCalendar(TeamMember member) {
        logger.info("Désynchronisation calendrier pour " + member.getUserId());
        // TODO: Implémenter la désynchronisation
    }
    
    private void updateCalendarPermissions(TeamMember member) {
        logger.info("Mise à jour permissions calendrier pour " + member.getUserId());
        // TODO: Implémenter la mise à jour des permissions
    }
}