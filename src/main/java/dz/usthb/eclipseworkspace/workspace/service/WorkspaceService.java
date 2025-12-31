package dz.usthb.eclipseworkspace.workspace.service;

import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.task.dao.DaoTask;
import dz.usthb.eclipseworkspace.workspace.dao.DaoAppUser;

import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.user.service.SecurityService;

import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardBuilder;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardDirector;

import dz.usthb.eclipseworkspace.workspace.service.components.TaskComponent;
import dz.usthb.eclipseworkspace.workspace.service.components.MemberComponent;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.user.dao.UserDao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceService {

    private final SecurityService security = SecurityService.getInstance();

    private final DaoWorkspace daoWorkspace;
    private final DaoTask daoTask;              // ✅ task module DAO
    private final DaoAppUser daoAppUser;

    private final WorkspaceDashboardDirector director;
    private final WorkspaceDashboardBuilder builder;

    // 🔑 AUTHORITATIVE SOURCE FOR ROLES
    private final TeamMemberDaoJdbc teamMemberDao = new TeamMemberDaoJdbc();
    private final UserDao userDao = new UserDao();

    public WorkspaceService(
            DaoWorkspace daoWorkspace,
            DaoTask daoTask,
            DaoAppUser daoAppUser,
            WorkspaceDashboardDirector director,
            WorkspaceDashboardBuilder builder
    ) {
        this.daoWorkspace = daoWorkspace;
        this.daoTask = daoTask;
        this.daoAppUser = daoAppUser;
        this.director = director;
        this.builder = builder;
    }

    // ==================================================
    // CREATE PROJECT
    // ==================================================
    public int createProject(
            String name,
            String description,
            long leaderUserId,
            List<String> memberEmails
    ) throws SQLException {

        security.requireAuthentication();

        int teamId = daoWorkspace.create(name, description, leaderUserId);

        TeamMember leader = new TeamMember();
        leader.setTeamId((long) teamId);
        leader.setUserId(leaderUserId);
        leader.setRole("LEAD");
        leader.setAddedAt(new java.sql.Date(System.currentTimeMillis()));
        teamMemberDao.create(leader);

        if (memberEmails != null) {
            for (String email : memberEmails) {
                if (email == null || email.isBlank()) continue;

                userDao.findByEmail(email.trim().toLowerCase())
                        .ifPresent(user -> {
                            try {
                                if (!teamMemberDao.exists((long) teamId, user.getUserId())) {
                                    TeamMember m = new TeamMember();
                                    m.setTeamId((long) teamId);
                                    m.setUserId(user.getUserId());
                                    m.setRole("MEMBER");
                                    m.setAddedAt(new java.sql.Date(System.currentTimeMillis()));
                                    teamMemberDao.create(m);
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        return teamId;
    }

    // ==================================================
    // BUILD SINGLE DASHBOARD
    // ==================================================
    public WorkspaceDashboard getDashboard(int teamId) throws SQLException {

        security.requireAuthentication();
        int currentUserId = security.getCurrentUserId().intValue();

        Workspace workspace = daoWorkspace.findById(teamId).orElse(null);
        if (workspace == null) return null;

        List<Task> tasks = daoTask.findByTeam(teamId);
        List<AppUser> members = daoAppUser.findMembersOfWorkspace(teamId);

        AppUser leader = null;
        TeamMember leaderMember =
                teamMemberDao.findLeaderByTeamId((long) teamId).orElse(null);

        if (leaderMember != null) {
            leader = daoAppUser.findById(leaderMember.getUserId().intValue());
        }

        WorkspaceComposite composite = new WorkspaceComposite();
        tasks.forEach(t -> composite.addComponent(new TaskComponent(t)));
        members.forEach(u -> composite.addComponent(new MemberComponent(u)));

        WorkspaceDashboard dashboard = director.buildDashboard(
                builder,
                workspace,
                tasks,
                members,
                leader,
                composite,
                computeProgress(tasks),
                getStartPct(tasks),
                getEndPct(tasks),
                buildDateLabels(tasks)
        );

        teamMemberDao.findByTeamAndUser((long) teamId, (long) currentUserId)
                .ifPresent(tm -> dashboard.setCurrentUserRole(tm.getRole()));

        // ================= CURRENT USER CONTEXT =================
AppUser currentUser = daoAppUser.findById(currentUserId);
dashboard.setCurrentUser(currentUser);

// Role already set correctly


        return dashboard;
    }

    // ==================================================
    // DASHBOARDS FOR USER
    // ==================================================
    public List<WorkspaceDashboard> getDashboardsForUser(int userId) throws SQLException {

        security.requireCanViewUser((long) userId);

        List<WorkspaceDashboard> dashboards = new ArrayList<>();
        List<TeamMember> memberships = teamMemberDao.findByUserId((long) userId);

        for (TeamMember tm : memberships) {
            WorkspaceDashboard dashboard = getDashboard(tm.getTeamId().intValue());
            if (dashboard == null) continue;

            dashboard.setCurrentUserRole(tm.getRole());
            dashboards.add(dashboard);
        }

        return dashboards;
    }

    // ==================================================
    // HELPERS
    // ==================================================
    private TaskProgress computeProgress(List<Task> tasks) {
        int todo = 0, inProgress = 0, done = 0;

        for (Task t : tasks) {
            switch (t.getStatus()) {
                case "TODO" -> todo++;
                case "IN_PROGRESS" -> inProgress++;
                case "DONE" -> done++;
            }
        }
        return new TaskProgress(todo, inProgress, done, tasks.size());
    }

    private List<Float> getStartPct(List<Task> tasks) {
        List<Float> result = new ArrayList<>();
        if (tasks.isEmpty()) return result;

        LocalDate min = tasks.stream()
                .map(Task::getStartDate)
                .filter(d -> d != null)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate max = tasks.stream()
                .map(Task::getDueDate)
                .filter(d -> d != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (min == null || max == null) return result;

        long total = ChronoUnit.DAYS.between(min, max);
        if (total == 0) total = 1;

        for (Task t : tasks) {
            if (t.getStartDate() == null) {
                result.add(0f);
            } else {
                long d = ChronoUnit.DAYS.between(min, t.getStartDate());
                result.add((float) d / total * 100f);
            }
        }
        return result;
    }

    private List<Float> getEndPct(List<Task> tasks) {
        List<Float> result = new ArrayList<>();
        if (tasks.isEmpty()) return result;

        LocalDate min = tasks.stream()
                .map(Task::getStartDate)
                .filter(d -> d != null)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate max = tasks.stream()
                .map(Task::getDueDate)
                .filter(d -> d != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (min == null || max == null) return result;

        long total = ChronoUnit.DAYS.between(min, max);
        if (total == 0) total = 1;

        for (Task t : tasks) {
            if (t.getDueDate() == null) {
                result.add(0f);
            } else {
                long d = ChronoUnit.DAYS.between(min, t.getDueDate());
                result.add((float) d / total * 100f);
            }
        }
        return result;
    }

    private List<String> buildDateLabels(List<Task> tasks) {
        List<String> labels = new ArrayList<>();
        if (tasks.isEmpty()) return labels;

        LocalDate min = tasks.stream()
                .map(Task::getStartDate)
                .filter(d -> d != null)
                .min(LocalDate::compareTo)
                .orElse(null);

        LocalDate max = tasks.stream()
                .map(Task::getDueDate)
                .filter(d -> d != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (min == null || max == null) return labels;

        long total = ChronoUnit.DAYS.between(min, max);
        for (int i = 0; i <= total; i += 7) {
            labels.add(min.plusDays(i).toString());
        }

        return labels;
    }
    // ==================================================
// DELETE WORKSPACE (LEAD ONLY)
// ==================================================
public void deleteWorkspace(int teamId) throws SQLException {

    System.out.println("🟣 [WorkspaceService] deleteWorkspace() START teamId=" + teamId);

    // 🔒 must be logged in
    try {
        security.requireAuthentication();
        System.out.println("🟣 Auth OK");
    } catch (Exception e) {
        System.err.println("🔴 Auth FAILED");
        throw e;
    }

    long currentUserId = security.getCurrentUserId();
    System.out.println("🟣 Current userId=" + currentUserId);

    // 1️⃣ verify membership
    TeamMember tm;
    try {
        tm = teamMemberDao
                .findByTeamAndUser((long) teamId, currentUserId)
                .orElseThrow(() ->
                        new SecurityException("User is not a member of this workspace")
                );

        System.out.println(
                "🟣 Membership OK — role=" + tm.getRole() +
                ", teamMemberId=" + tm.getTeamMemberId()
        );

    } catch (Exception e) {
        System.err.println("🔴 Membership check FAILED");
        throw e;
    }

    // 2️⃣ only LEAD can delete
    if (!"LEAD".equals(tm.getRole())) {
        System.err.println("🔴 Permission denied — role=" + tm.getRole());
        throw new SecurityException("Only LEAD can delete workspace");
    }

    System.out.println("🟣 Role check OK (LEAD)");

    // 3️⃣ delete tasks first (FK safety)
    try {
        System.out.println("🟣 Deleting tasks for teamId=" + teamId);
        daoTask.deleteByTeam(teamId);
        System.out.println("🟣 Tasks deleted");
    } catch (Exception e) {
        System.err.println("🔴 Task deletion FAILED");
        throw e;
    }

    // 4️⃣ delete team members
    try {
        System.out.println("🟣 Deleting team members for teamId=" + teamId);
        teamMemberDao.deleteByTeamId((long) teamId);
        System.out.println("🟣 Team members deleted");
    } catch (Exception e) {
        System.err.println("🔴 Team member deletion FAILED");
        throw e;
    }

    // 5️⃣ delete workspace itself
    try {
        System.out.println("🟣 Deleting workspace row teamId=" + teamId);
        daoWorkspace.delete(teamId);
        System.out.println("🟣 Workspace row deleted");
    } catch (Exception e) {
        System.err.println("🔴 Workspace deletion FAILED");
        throw e;
    }

    System.out.println("✅ [WorkspaceService] deleteWorkspace() SUCCESS teamId=" + teamId);
}

}
