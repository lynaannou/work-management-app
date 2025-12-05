package dz.usthb.eclipseworkspace.team.service;

import dz.usthb.eclipseworkspace.team.model.TeamMember;
import java.util.ArrayList;
import java.util.List;

public class TeamSubject {
    private final List<TeamObserver> observers = new ArrayList<>();
    
    public void addObserver(TeamObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(TeamObserver observer) {
        observers.remove(observer);
    }
    
    protected void notifyMemberAdded(TeamMember newMember) {
        for (TeamObserver observer : observers) {
            observer.onMemberAdded(newMember);
        }
    }
    
    protected void notifyMemberRemoved(TeamMember removedMember) {
        for (TeamObserver observer : observers) {
            observer.onMemberRemoved(removedMember);
        }
    }
    
    protected void notifyMemberRoleChanged(TeamMember member, String oldRole) {
        for (TeamObserver observer : observers) {
            observer.onMemberRoleChanged(member, oldRole);
        }
    }
}