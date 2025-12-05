package dz.usthb.eclipseworkspace.team.service;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

public interface TeamObserver {
    void onMemberAdded(TeamMember newMember);
    void onMemberRemoved(TeamMember removedMember);
    void onMemberRoleChanged(TeamMember member, String oldRole);
}