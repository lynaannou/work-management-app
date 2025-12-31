package dz.usthb.eclipseworkspace.workspace.service.builder;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import java.util.List;

public class WorkspaceDashboardBuilder {

    private Workspace workspace;
    private List<Task> tasks;
    private List<AppUser> members;
    private AppUser leader;
    private WorkspaceComposite composite;
    private TaskProgress progress;
    private List<Float> startPcts;
    private List<Float> endPcts;
    private List<String> dateLabels;

    // ======================================
    // RESET — MUST be called before reuse
    // ======================================
    public void reset() {
        workspace = null;
        tasks = null;
        members = null;
        leader = null;
        composite = null;
        progress = null;
        startPcts = null;
        endPcts = null;
        dateLabels = null;
    }

    // ======================================
    // BUILDER STEPS
    // ======================================
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setMembers(List<AppUser> members) {
        this.members = members;
    }

    public void setLeader(AppUser leader) {
        this.leader = leader;
    }

    public void setComposite(WorkspaceComposite composite) {
        this.composite = composite;
    }

    public void setProgress(TaskProgress progress) {
        this.progress = progress;
    }

    public void setStartPcts(List<Float> startPcts) {
        this.startPcts = startPcts;
    }

    public void setEndPcts(List<Float> endPcts) {
        this.endPcts = endPcts;
    }

    public void setDateLabels(List<String> dateLabels) {
        this.dateLabels = dateLabels;
    }

    // ======================================
    // FINAL PRODUCT
    // ======================================
    public WorkspaceDashboard build() {

        // 🔥 HARD SAFETY CHECKS (VERY IMPORTANT)
        if (workspace == null) {
            throw new IllegalStateException("WorkspaceDashboardBuilder: workspace is NULL");
        }

        if (tasks == null) {
            throw new IllegalStateException("WorkspaceDashboardBuilder: tasks list is NULL");
        }

        if (members == null) {
            throw new IllegalStateException("WorkspaceDashboardBuilder: members list is NULL");
        }

        if (progress == null) {
            throw new IllegalStateException("WorkspaceDashboardBuilder: progress is NULL");
        }

        if (startPcts == null || endPcts == null || dateLabels == null) {
            throw new IllegalStateException("WorkspaceDashboardBuilder: timeline data is NULL");
        }

        WorkspaceDashboard dashboard = new WorkspaceDashboard();

        dashboard.setWorkspace(workspace);
        dashboard.setTasks(tasks);
        dashboard.setMembers(members);
        dashboard.setLeader(leader);
        dashboard.setComposite(composite);
        dashboard.setProgress(progress);
        dashboard.setStartPcts(startPcts);
        dashboard.setEndPcts(endPcts);
        dashboard.setDateLabels(dateLabels);

        return dashboard;
    }
}
