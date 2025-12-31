package dz.usthb.eclipseworkspace.team.dao;

import dz.usthb.eclipseworkspace.team.DTO.TeamMemberView;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamMemberDaoJdbc implements TeamMemberDao {
    @Override
public boolean existsForTask(Long teamMemberId, Long taskId) throws SQLException {

    String sql = """
        SELECT 1
        FROM task t
        WHERE t.task_id = ?
          AND t.team_member_id = ?
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, taskId);
        ps.setLong(2, teamMemberId);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

    
    @Override
    public Long create(TeamMember teamMember) throws SQLException {
        String sql = "INSERT INTO team_member (team_id, user_id, role, added_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, teamMember.getTeamId());
            ps.setLong(2, teamMember.getUserId());
            ps.setString(3, teamMember.getRole());
            ps.setDate(4, teamMember.getAddedAt() != null ? teamMember.getAddedAt() : new Date(System.currentTimeMillis()));
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Échec de la création du membre d'équipe");
    }
    
    @Override
    public Optional<TeamMember> findById(Long teamMemberId) throws SQLException {
        String sql = "SELECT tm.*, COALESCE(COUNT(t.task_id), 0) as task_count " +
                    "FROM team_member tm " +
                    "LEFT JOIN task t ON tm.team_member_id = t.team_member_id " +
                    "WHERE tm.team_member_id = ? " +
                    "GROUP BY tm.team_member_id";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamMemberId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeamMember(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public List<TeamMember> findByTeamId(Long teamId) throws SQLException {
        String sql = "SELECT tm.*, COALESCE(COUNT(t.task_id), 0) as task_count " +
                    "FROM team_member tm " +
                    "LEFT JOIN task t ON tm.team_member_id = t.team_member_id " +
                    "WHERE tm.team_id = ? " +
                    "GROUP BY tm.team_member_id " +
                    "ORDER BY tm.role DESC, tm.added_at";
        
        List<TeamMember> members = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToTeamMember(rs));
                }
            }
        }
        return members;
    }
    
    @Override
    public List<TeamMember> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT tm.*, COALESCE(COUNT(t.task_id), 0) as task_count " +
                    "FROM team_member tm " +
                    "LEFT JOIN task t ON tm.team_member_id = t.team_member_id " +
                    "WHERE tm.user_id = ? " +
                    "GROUP BY tm.team_member_id";
        
        List<TeamMember> members = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToTeamMember(rs));
                }
            }
        }
        return members;
    }
    @Override
public Optional<TeamMember> findLeaderByTeamId(Long teamId) throws SQLException {
    String sql = """
        SELECT * FROM team_member
        WHERE team_id = ? AND role = 'LEAD'
        LIMIT 1
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, teamId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapResultSetToTeamMember(rs));
            }
        }
    }
    return Optional.empty();
}

    @Override
    public boolean updateRole(Long teamMemberId, String newRole) throws SQLException {
        String sql = "UPDATE team_member SET role = ? WHERE team_member_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newRole);
            ps.setLong(2, teamMemberId);
            
            return ps.executeUpdate() > 0;
        }
    }
    // ==========================
// DELETE ALL MEMBERS OF A TEAM
// ==========================
public void deleteByTeamId(Long teamId) throws SQLException {

    System.out.println("🧹 [TeamMemberDao] deleteByTeamId team_id = " + teamId);

    String sql = "DELETE FROM team_member WHERE team_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, teamId);
        ps.executeUpdate();
    }
}

    @Override
    public boolean delete(Long teamMemberId) throws SQLException {
        String sql = "DELETE FROM team_member WHERE team_member_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamMemberId);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean exists(Long teamId, Long userId) throws SQLException {
        String sql = "SELECT 1 FROM team_member WHERE team_id = ? AND user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamId);
            ps.setLong(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    @Override
    public Optional<TeamMember> findByTeamAndUser(Long teamId, Long userId) throws SQLException {
        String sql = "SELECT tm.*, COALESCE(COUNT(t.task_id), 0) as task_count " +
                    "FROM team_member tm " +
                    "LEFT JOIN task t ON tm.team_member_id = t.team_member_id " +
                    "WHERE tm.team_id = ? AND tm.user_id = ? " +
                    "GROUP BY tm.team_member_id";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamId);
            ps.setLong(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeamMember(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public int countMembersByTeam(Long teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM team_member WHERE team_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    @Override
    public List<TeamMember> findOverloadedMembers(Long teamId) throws SQLException {
        String sql = "SELECT tm.*, COALESCE(COUNT(t.task_id), 0) as task_count " +
                    "FROM team_member tm " +
                    "LEFT JOIN task t ON tm.team_member_id = t.team_member_id " +
                    "WHERE tm.team_id = ? " +
                    "GROUP BY tm.team_member_id " +
                    "HAVING COALESCE(COUNT(t.task_id), 0) >= 5";
        
        List<TeamMember> overloaded = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, teamId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    overloaded.add(mapResultSetToTeamMember(rs));
                }
            }
        }
        return overloaded;
    }

    @Override
    public String getRoleByUserId(Long userId) throws SQLException {
        String role = null;
        String sql = "SELECT role FROM team_member WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        }

        return role; // returns null if user is not a member of any team
    }
    
    private TeamMember mapResultSetToTeamMember(ResultSet rs) throws SQLException {
        TeamMember member = new TeamMember();
        member.setTeamMemberId(rs.getLong("team_member_id"));
        member.setTeamId(rs.getLong("team_id"));
        member.setUserId(rs.getLong("user_id"));
        member.setRole(rs.getString("role"));
        member.setAddedAt(rs.getDate("added_at"));
        member.setTaskCount(rs.getInt("task_count"));
        return member;
    }
    @Override
public List<TeamMemberView> findTeamMembersView(Long teamId) throws SQLException {

    System.out.println("🟥 DAO.findTeamMembersView ENTER teamId=" + teamId);

    String sql = """
        SELECT
            tm.team_member_id,
            tm.user_id,
            tm.role,
            COALESCE(COUNT(t.task_id), 0) AS task_count,
            u.first_name,
            u.last_name
        FROM team_member tm
        JOIN app_user u ON u.user_id = tm.user_id
        LEFT JOIN task t ON t.team_member_id = tm.team_member_id
        WHERE tm.team_id = ?
        GROUP BY tm.team_member_id, u.first_name, u.last_name
        ORDER BY tm.role DESC, tm.added_at
    """;

    List<TeamMemberView> result = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        System.out.println("🟥 DAO SQL prepared");

        ps.setLong(1, teamId);

        try (ResultSet rs = ps.executeQuery()) {
            int rowCount = 0;

            while (rs.next()) {
                rowCount++;

                TeamMemberView v = new TeamMemberView();
                v.setTeamMemberId(rs.getLong("team_member_id"));
                v.setUserId(rs.getLong("user_id"));
                v.setFirstName(rs.getString("first_name"));
                v.setLastName(rs.getString("last_name"));
                v.setRole(rs.getString("role"));
                v.setTaskCount(rs.getInt("task_count"));

                System.out.println("🟥 DAO ROW " + rowCount + " → "
                        + v.getFirstName() + " " + v.getLastName()
                        + " (" + v.getRole() + ")");

                result.add(v);
            }

            System.out.println("🟥 DAO RESULT SIZE=" + result.size());
        }
    }

    return result;
}
@Override
public boolean belongsToTeam(Long teamMemberId, Long teamId) throws SQLException {

    System.out.println("🔍 [TeamMemberDaoJdbc] belongsToTeam()");
    System.out.println("   teamMemberId=" + teamMemberId);
    System.out.println("   teamId=" + teamId);

    String sql = """
        SELECT 1
        FROM team_member
        WHERE team_member_id = ?
          AND team_id = ?
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, teamMemberId);
        ps.setLong(2, teamId);

        try (ResultSet rs = ps.executeQuery()) {
            boolean exists = rs.next();
            System.out.println("   result=" + exists);
            return exists;
        }
    }
}

}
