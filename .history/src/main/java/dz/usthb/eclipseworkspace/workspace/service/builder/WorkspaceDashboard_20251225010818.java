package dz.usthb.eclipseworkspace.workspace.service.builder;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.workspace.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;


import java.util.List;

public class WorkspaceDashboard {

    private Workspace workspace;
    private List<Task> tasks;
    private List<AppUser> members;
    private WorkspaceComposite composite;
    private TaskProgress progress;
    private AppUser leader; // <- Team leader user (full info)
    private List<Float> startPcts;
    private List<Float> endPcts;
    private List<String> dateLabels;
    


    // ----------- GETTERS & SETTERS ------------

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<AppUser> getMembers() {
        return members;
    }

    public void setMembers(List<AppUser> members) {
        this.members = members;
    }

    public WorkspaceComposite getComposite() {
        return composite;
    }

    public void setComposite(WorkspaceComposite composite) {
        this.composite = composite;
    }

    public TaskProgress getProgress() {
        return progress;
    }

    public void setProgress(TaskProgress progress) {
        this.progress = progress;
    }

    public AppUser getLeader() {
        return leader;
    }

    public void setLeader(AppUser leader) {
        this.leader = leader;
    }
    public List<Float> getStartPcts() { return startPcts; }
public List<Float> getEndPcts() { return endPcts; }
public List<String> getDateLabels() { return dateLabels; }

public void setStartPcts(List<Float> startPcts) { this.startPcts = startPcts; }
public void setEndPcts(List<Float> endPcts) { this.endPcts = endPcts; }
public void setDateLabels(List<String> dateLabels) { this.dateLabels = dateLabels; }
public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
