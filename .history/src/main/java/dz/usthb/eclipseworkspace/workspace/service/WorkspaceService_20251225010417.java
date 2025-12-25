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
import dz.usthb.eclipseworkspace.user.model.User;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class WorkspaceService {

    private final SecurityService security = SecurityService.getInstance();

    private DaoWorkspace daoWorkspace;
    private DaoTask daoTask;
    private DaoAppUser daoAppUser;

    private WorkspaceDashboardDirector director;
    private WorkspaceDashboardBuilder builder;

    // 🔽 ONLY ADDITION
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

    // ======================================
    // CREATE PROJECT / WORKSPACE / TEAM
    // ======================================
    public int createProject(
            String name,
            String description,
            long leaderUserId,
            List<String> memberEmails
    ) throws SQLException {

        // 🔐 Must be authenticated
        security.requireAuthentication();

        System.out.println("🆕 Creating project");
        System.out.println("   name = " + name);
        System.out.println("   leaderUserId = " + leaderUserId);

        // 1️⃣ Create workspace (team)
        int teamId = daoWorkspace.create(name, description, leaderUserId);

        // 2️⃣ Insert LEADER as team member
        TeamMember leader = new TeamMember();
        leader.setTeamId((long) teamId);
        leader.setUserId(leaderUserId);
        leader.setRole("LEAD");
        leader.setAddedAt(new Date(System.currentTimeMillis()));

        teamMemberDao.create(leader);

        // 3️⃣ Insert MEMBERS by email (if they exist)
        if (memberEmails != null) {
            for (String email : memberEmails) {

                if (email == null || email.isBlank())
                    continue;

                userDao.findByEmail(email.trim().toLowerCase())
                        .ifPresent(user -> {
                            try {
                                // Avoid duplicates
                                if (!teamMemberDao.exists((long) teamId, user.getUserId())) {
                                    TeamMember member = new TeamMember();
                                    member.setTeamId((long) teamId);
                                    member.setUserId(user.getUserId());
                                    member.setRole("MEMBER");
                                    member.setAddedAt(new Date(System.currentTimeMillis()));

                                    teamMemberDao.create(member);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }

        System.out.println("✅ Project created successfully with team_id = " + teamId);
        return teamId;
    }

    // ======================================
    // MAIN ENTRY: BUILD FULL DASHBOARD
    // ======================================
    public WorkspaceDashboard getDashboard(int workspaceId) throws SQLException {

        security.requireAuthentication();

        System.out.println("\n====================================");
        System.out.println("DEBUG getDashboard(" + workspaceId + ")");
        System.out.println("====================================");
        System.out.println("📦 Loading dashboard for teamId = " + workspaceId);

        Workspace workspace = daoWorkspace.findById(workspaceId).orElse(null);
        System.out.println("Workspace = " + workspace);

        if (workspace == null) {
            System.out.println("ERROR: No workspace found for ID = " + workspaceId);
            return null;
        }

        List<Task> tasks = daoTask.findByTeam(workspaceId);

        List<AppUser> members = daoAppUser.findMembersOfWorkspace(workspaceId);

        AppUser leader = members.isEmpty() ? null : members.get(0);

        WorkspaceComposite composite = new WorkspaceComposite();
        tasks.forEach(t -> composite.addComponent(new TaskComponent(t)));
        members.forEach(u -> composite.addComponent(new MemberComponent(u)));

        TaskProgress progress = computeProgress(tasks);

        List<Float> startPcts = getStartPct(tasks);
        List<Float> endPcts = getEndPct(tasks);
        List<String> dateLabels = buildDateLabels(tasks);

        return director.buildDashboard(
                builder,
                workspace,
                tasks,
                members,
                leader,
                composite,
                progress,
                startPcts,
                endPcts,
                dateLabels
        );
    }

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

        Date globalMin = tasks.stream()
                .map(Task::getStartDate)
                .min(Date::compareTo)
                .orElse(null);

        Date globalMax = tasks.stream()
                .map(Task::getEndDate)
                .max(Date::compareTo)
                .orElse(null);

        MinMaxDate mm = new MinMaxDate(globalMin, globalMax);
        int total = mm.getDuration();

        List<Float> result = new ArrayList<>();
        for (Task t : tasks) {
            int offset = mm.getAnyDuration(globalMin, t.getStartDate());
            float pct = (float) offset / total * 100f;
            result.add(pct);
        }
        return result;
    }

    private List<Float> getEndPct(List<Task> tasks) {

        Date globalMin = tasks.stream()
                .map(Task::getStartDate)
                .min(Date::compareTo)
                .orElse(null);

        Date globalMax = tasks.stream()
                .map(Task::getEndDate)
                .max(Date::compareTo)
                .orElse(null);

        MinMaxDate mm = new MinMaxDate(globalMin, globalMax);
        int total = mm.getDuration();

        List<Float> result = new ArrayList<>();
        for (Task t : tasks) {
            int offset = mm.getAnyDuration(globalMin, t.getEndDate());
            float pct = (float) offset / total * 100f;
            result.add(pct);
        }
        return result;
    }

    private List<String> buildDateLabels(List<Task> tasks) {

        List<String> labels = new ArrayList<>();

        Date globalMin = tasks.stream()
                .map(Task::getStartDate)
                .min(Date::compareTo)
                .orElse(null);

        Date globalMax = tasks.stream()
                .map(Task::getEndDate)
                .max(Date::compareTo)
                .orElse(null);

        if (globalMin == null || globalMax == null)
            return labels;

        MinMaxDate mm = new MinMaxDate(globalMin, globalMax);
        int totalDays = mm.getDuration();

        for (int i = 0; i < totalDays; i += 7) {
            long ms = globalMin.getTime() + (i * 24L * 60 * 60 * 1000);
            labels.add(new Date(ms).toString());
        }

        labels.add(globalMax.toString());
        return labels;
    }

    public List<WorkspaceDashboard> getDashboardsForUser(int userId) throws SQLException {
        System.out.println(
  "DEBUG ROLE → teamId=" + teamId +
  " userId=" + userId +
  " role=" + teamMember.getRole()
);


        security.requireCanViewUser((long) userId);

        List<Workspace> workspaces = daoWorkspace.findByUser(userId);
        List<WorkspaceDashboard> dashboards = new ArrayList<>();

        for (Workspace ws : workspaces) {
            WorkspaceDashboard dashboard = getDashboard(ws.getTeamId());
            if (dashboard != null) {
                dashboards.add(dashboard);
            }
        }
        return dashboards;
    }
}
