package dz.usthb.eclipseworkspace.workspace.service.builder;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.workspace.model.Task;
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

    // ------------------------------
    // Reset builder between dashboards
    // ------------------------------
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

    // ------------------------------
    // Builder SETTERS (step by step)
    // ------------------------------
    public void setWorkspace(Workspace w) {
        this.workspace = w;
    }

    public void setTasks(List<Task> t) {
        this.tasks = t;
    }

    public void setMembers(List<AppUser> m) {
        this.members = m;
    }

    public void setLeader(AppUser leader) {
        this.leader = leader;
    }

    public void setComposite(WorkspaceComposite c) {
        this.composite = c;
    }

    public void setProgress(TaskProgress p) {
        this.progress = p;
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

    // ------------------------------
    // Final product construction
    // ------------------------------
    public WorkspaceDashboard build() {
        WorkspaceDashboard d = new WorkspaceDashboard();

        d.setWorkspace(workspace);
        d.setTasks(tasks);
        d.setMembers(members);
        d.setLeader(leader);
        d.setComposite(composite);
        d.setProgress(progress);
        d.setStartPcts(startPcts);
        d.setEndPcts(endPcts);
        d.setDateLabels(dateLabels);

        return d;
    }
}
