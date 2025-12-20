package dz.usthb.eclipseworkspace.team.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

public class TeamMemberDaoMock implements TeamMemberDao {
    private final Map<Long, TeamMember> members = new HashMap<>();
    private final Map<String, TeamMember> teamUserIndex = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Long create(TeamMember teamMember) throws Exception {
        Long newId = idGenerator.getAndIncrement();
        teamMember.setTeamMemberId(newId);
        members.put(newId, teamMember);
        
        String key = teamMember.getTeamId() + "-" + teamMember.getUserId();
        teamUserIndex.put(key, teamMember);
        
        return newId;
    }
    
    @Override
    public Optional<TeamMember> findById(Long teamMemberId) throws Exception {
        return Optional.ofNullable(members.get(teamMemberId));
    }
    
    @Override
    public List<TeamMember> findByTeamId(Long teamId) throws Exception {
        List<TeamMember> result = new ArrayList<>();
        for (TeamMember member : members.values()) {
            if (member.getTeamId().equals(teamId)) {
                result.add(member);
            }
        }
        return result;
    }
    
    @Override
    public List<TeamMember> findByUserId(Long userId) throws Exception {
        List<TeamMember> result = new ArrayList<>();
        for (TeamMember member : members.values()) {
            if (member.getUserId().equals(userId)) {
                result.add(member);
            }
        }
        return result;
    }
    
    @Override
    public boolean updateRole(Long teamMemberId, String newRole) throws Exception {
        TeamMember member = members.get(teamMemberId);
        if (member != null) {
            member.setRole(newRole);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean delete(Long teamMemberId) throws Exception {
        TeamMember removed = members.remove(teamMemberId);
        if (removed != null) {
            String key = removed.getTeamId() + "-" + removed.getUserId();
            teamUserIndex.remove(key);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean exists(Long teamId, Long userId) throws Exception {
        String key = teamId + "-" + userId;
        return teamUserIndex.containsKey(key);
    }
    
    @Override
    public Optional<TeamMember> findByTeamAndUser(Long teamId, Long userId) throws Exception {
        String key = teamId + "-" + userId;
        return Optional.ofNullable(teamUserIndex.get(key));
    }
    
    @Override
    public int countMembersByTeam(Long teamId) throws Exception {
        int count = 0;
        for (TeamMember member : members.values()) {
            if (member.getTeamId().equals(teamId)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public List<TeamMember> findOverloadedMembers(Long teamId) throws Exception {
        List<TeamMember> result = new ArrayList<>();
        for (TeamMember member : members.values()) {
            if (member.getTeamId().equals(teamId) && 
                member.getTaskCount() != null && 
                member.getTaskCount() >= 5) {
                result.add(member);
            }
        }
        return result;
    }
}