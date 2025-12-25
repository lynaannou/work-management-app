package dz.usthb.eclipseworkspace.workspace.service;

import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.workspace.dao.DaoTask;
import dz.usthb.eclipseworkspace.workspace.dao.DaoAppUser;

import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.model.Task;
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

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceService {

    private final SecurityService security = SecurityService.getInstance();

    private final DaoWorkspace daoWorkspace;
    private final DaoTask daoTask;
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

        // LEADER
        TeamMember leader = new TeamMember();
        leader.setTeamId((long) teamId);
        leader.setUserId(leaderUserId);
        leader.setRole("LEAD");
        leader.setAddedAt(new Date(System.currentTimeMillis()));
        teamMemberDao.create(leader);

        // MEMBERS
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
                                    m.setAddedAt(new Date(System.currentTimeMillis()));
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
    // BUILD SINGLE DASHBOARD (NO ROLE GUESSING)
    // ==================================================
    public WorkspaceDashboard getDashboard(int teamId) throws SQLException {

    security.requireAuthentication();

    int currentUserId = security.getCurrentUserId().intValue(); // 🔑 ADD THIS

    Workspace workspace = daoWorkspace.findById(teamId).orElse(null);
    if (workspace == null) return null;

    List<Task> tasks = daoTask.findByTeam(teamId);
    List<AppUser> members = daoAppUser.findMembersOfWorkspace(teamId);

    // ✅ LEADER
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

    // 🔥 THIS WAS MISSING
    teamMemberDao.findByTeamAndUser((long) teamId, (long) currentUserId)
            .ifPresent(tm -> dashboard.setCurrentUserRole(tm.getRole()));

    return dashboard;
}


    // ==================================================
    // DASHBOARDS FOR USER (ROLE INJECTION)
    // ==================================================
    public List<WorkspaceDashboard> getDashboardsForUser(int userId) throws SQLException {

        security.requireCanViewUser((long) userId);

        List<WorkspaceDashboard> dashboards = new ArrayList<>();

        // 🔑 ROLE SOURCE
        List<TeamMember> memberships = teamMemberDao.findByUserId((long) userId);

        for (TeamMember tm : memberships) {

            int teamId = tm.getTeamId().intValue();
            String role = tm.getRole();

            System.out.println(
                    "DEBUG ROLE → teamId=" + teamId +
                    " userId=" + userId +
                    " role=" + role
            );

            WorkspaceDashboard dashboard = getDashboard(teamId);
            if (dashboard == null) continue;

            // ✅ ROLE INJECTION
            dashboard.setCurrentUserRole(role);

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

        Date min = tasks.stream().map(Task::getStartDate).min(Date::compareTo).orElse(null);
        Date max = tasks.stream().map(Task::getEndDate).max(Date::compareTo).orElse(null);
        if (min == null || max == null) return result;

        MinMaxDate mm = new MinMaxDate(min, max);
        int total = mm.getDuration();

        for (Task t : tasks) {
            result.add((float) mm.getAnyDuration(min, t.getStartDate()) / total * 100f);
        }
        return result;
    }

    private List<Float> getEndPct(List<Task> tasks) {
        List<Float> result = new ArrayList<>();
        if (tasks.isEmpty()) return result;

        Date min = tasks.stream().map(Task::getStartDate).min(Date::compareTo).orElse(null);
        Date max = tasks.stream().map(Task::getEndDate).max(Date::compareTo).orElse(null);
        if (min == null || max == null) return result;

        MinMaxDate mm = new MinMaxDate(min, max);
        int total = mm.getDuration();

        for (Task t : tasks) {
            result.add((float) mm.getAnyDuration(min, t.getEndDate()) / total * 100f);
        }
        return result;
    }

    private List<String> buildDateLabels(List<Task> tasks) {
        List<String> labels = new ArrayList<>();
        if (tasks.isEmpty()) return labels;

        Date min = tasks.stream().map(Task::getStartDate).min(Date::compareTo).orElse(null);
        Date max = tasks.stream().map(Task::getEndDate).max(Date::compareTo).orElse(null);
        if (min == null || max == null) return labels;

        MinMaxDate mm = new MinMaxDate(min, max);
        int total = mm.getDuration();

        for (int i = 0; i < total; i += 7) {
            labels.add(new Date(min.getTime() + i * 86400000L).toString());
        }
        labels.add(max.toString());
        return labels;
    }
}
