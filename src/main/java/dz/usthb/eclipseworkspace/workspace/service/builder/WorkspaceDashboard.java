package dz.usthb.eclipseworkspace.workspace.service.builder;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import java.util.List;

/**
 * DTO sent to the frontend (JavaFX WebView)
 */
public class WorkspaceDashboard {

    // ================= CORE DATA =================

    private Workspace workspace;
    private List<Task> tasks;
    private List<AppUser> members;
    private WorkspaceComposite composite;
    private TaskProgress progress;

    /** Team leader (informational only) */
    private AppUser leader;

    /** ✅ CURRENT AUTHENTICATED USER (navbar, permissions) */
    private AppUser currentUser;

    // ================= TIMELINE =================

    private List<Float> startPcts;
    private List<Float> endPcts;
    private List<String> dateLabels;

    /** 🔑 ROLE OF CURRENT USER IN THIS WORKSPACE */
    private String currentUserRole;

    // ================= GETTERS / SETTERS =================

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

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(AppUser currentUser) {
        this.currentUser = currentUser;
    }

    public List<Float> getStartPcts() {
        return startPcts;
    }

    public void setStartPcts(List<Float> startPcts) {
        this.startPcts = startPcts;
    }

    public List<Float> getEndPcts() {
        return endPcts;
    }

    public void setEndPcts(List<Float> endPcts) {
        this.endPcts = endPcts;
    }

    public List<String> getDateLabels() {
        return dateLabels;
    }

    public void setDateLabels(List<String> dateLabels) {
        this.dateLabels = dateLabels;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(String currentUserRole) {
        this.currentUserRole = currentUserRole;
    }
}
