package dz.usthb.eclipseworkspace.team.dao;

import dz.usthb.eclipseworkspace.team.model.TeamMember;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TeamMemberDao {
    Long create(TeamMember teamMember) throws Exception;
    Optional<TeamMember> findById(Long teamMemberId) throws Exception;
    List<TeamMember> findByTeamId(Long teamId) throws Exception;
    List<TeamMember> findByUserId(Long userId) throws Exception;
    boolean updateRole(Long teamMemberId, String newRole) throws Exception;
    boolean delete(Long teamMemberId) throws Exception;
    boolean exists(Long teamId, Long userId) throws Exception;
    Optional<TeamMember> findByTeamAndUser(Long teamId, Long userId) throws Exception;
    int countMembersByTeam(Long teamId) throws Exception;
    List<TeamMember> findOverloadedMembers(Long teamId) throws Exception;

    String getRoleByUserId(Long userId) throws SQLException;


}