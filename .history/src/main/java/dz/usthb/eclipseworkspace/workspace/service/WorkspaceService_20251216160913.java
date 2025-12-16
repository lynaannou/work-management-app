package dz.usthb.eclipseworkspace.workspace.service;

import dz.usthb.eclipseworkspace.workspace.dao.DaoWorkspace;
import dz.usthb.eclipseworkspace.workspace.dao.DaoTask;
import dz.usthb.eclipseworkspace.workspace.dao.DaoAppUser;

import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;

import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardBuilder;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboardDirector;

import dz.usthb.eclipseworkspace.workspace.service.components.TaskComponent;
import dz.usthb.eclipseworkspace.workspace.service.components.MemberComponent;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComponent;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.util.stream.Collectors;


public class WorkspaceService {

    private DaoWorkspace daoWorkspace;
    private DaoTask daoTask;
    private DaoAppUser daoAppUser;

    private WorkspaceDashboardDirector director;
    private WorkspaceDashboardBuilder builder;

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
    // MAIN ENTRY: BUILD FULL DASHBOARD
    // ======================================
    public WorkspaceDashboard getDashboard(int workspaceId) throws SQLException {

    System.out.println("\n====================================");
    System.out.println("DEBUG getDashboard(" + workspaceId + ")");
    System.out.println("====================================");
        System.out.println("📦 Loading dashboard for teamId = " + teamId);


    Workspace workspace = daoWorkspace.findById(workspaceId).orElse(null);
    System.out.println("Workspace = " + workspace);

    if (workspace == null) {
        System.out.println("ERROR: No workspace found for ID = " + workspaceId);
        return null;
    }


    List<Task> tasks = daoTask.findByTeam(workspaceId);
    System.out.println("Tasks loaded = " + tasks.size());
    for (Task t : tasks) {
        System.out.println("  Task: " + t.getTask_id() + " | " + t.getTitle() + " | " + t.getStartDate() + " → " + t.getEndDate());
    }

    List<AppUser> members = daoAppUser.findMembersOfWorkspace(workspaceId);
    System.out.println("Members loaded = " + members.size());
    for (AppUser u : members) {
        System.out.println("  Member: " + u.getUser_id() + " | " + u.getFirstName() + " " + u.getLastName());
    }

    AppUser leader = members.isEmpty() ? null : members.get(0);
    System.out.println("Leader = " + leader);

    WorkspaceComposite composite = new WorkspaceComposite();
    tasks.forEach(t -> composite.addComponent(new TaskComponent(t)));
    members.forEach(u -> composite.addComponent(new MemberComponent(u)));

    TaskProgress progress = computeProgress(tasks);
    System.out.println("Progress = " + progress.getDone() + "/" + progress.getTotal());

    List<Float> startPcts = getStartPct(tasks);
    List<Float> endPcts = getEndPct(tasks);

    System.out.println("StartPcts = " + startPcts);
    System.out.println("EndPcts   = " + endPcts);

    List<String> dateLabels = buildDateLabels(tasks);
    System.out.println("Date labels = " + dateLabels);

    WorkspaceDashboard dashboard = director.buildDashboard(
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

    System.out.println("DEBUG: Dashboard built successfully.");
    return dashboard;
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



    // ======================================
    // TIMELINE PERCENTAGES
    // ======================================
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


    // ======================================
    // TIMELINE DATE LABELS
    // ======================================
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

        // Label each week
        for (int i = 0; i < totalDays; i += 7) {
            long ms = globalMin.getTime() + (i * 24L * 60 * 60 * 1000);
            Date step = new Date(ms);
            labels.add(step.toString());
        }

        // Always end with max
        labels.add(globalMax.toString());

        return labels;
    }
    public List<WorkspaceDashboard> getDashboardsForUser(int userId) throws SQLException {

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
