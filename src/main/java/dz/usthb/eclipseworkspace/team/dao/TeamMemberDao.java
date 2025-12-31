package dz.usthb.eclipseworkspace.team.dao;

import dz.usthb.eclipseworkspace.team.DTO.TeamMemberView;
import dz.usthb.eclipseworkspace.team.model.TeamMember;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TeamMemberDao {

    Long create(TeamMember teamMember) throws SQLException;

    Optional<TeamMember> findById(Long teamMemberId) throws SQLException;

    List<TeamMember> findByTeamId(Long teamId) throws SQLException;

    List<TeamMember> findByUserId(Long userId) throws SQLException;

    boolean updateRole(Long teamMemberId, String newRole) throws SQLException;

    boolean delete(Long teamMemberId) throws SQLException;

    // ✅ already used by createTask
    boolean exists(Long teamId, Long userId) throws SQLException;

    // ✅ NEW — REQUIRED FOR TASK SAFETY
    boolean existsForTask(Long teamMemberId, Long taskId) throws Exception;

    Optional<TeamMember> findByTeamAndUser(Long teamId, Long userId) throws SQLException;
    void deleteByTeamId(Long teamId) throws SQLException;

    int countMembersByTeam(Long teamId) throws SQLException;

    List<TeamMember> findOverloadedMembers(Long teamId) throws SQLException;

    Optional<TeamMember> findLeaderByTeamId(Long teamId) throws SQLException;

    String getRoleByUserId(Long userId) throws SQLException;
    List<TeamMemberView> findTeamMembersView(Long teamId) throws SQLException;
    boolean belongsToTeam(Long teamMemberId, Long teamId) throws SQLException;
}