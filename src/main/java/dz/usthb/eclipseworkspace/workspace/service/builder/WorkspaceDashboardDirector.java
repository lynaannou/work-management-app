package dz.usthb.eclipseworkspace.workspace.service.builder;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import java.util.List;

public class WorkspaceDashboardDirector {

    public WorkspaceDashboard buildDashboard(
            WorkspaceDashboardBuilder builder,
            Workspace workspace,
            List<Task> tasks,
            List<AppUser> members,
            AppUser leader,
            WorkspaceComposite composite,
            TaskProgress progress,
            List<Float> startPcts,
            List<Float> endPcts,
            List<String> dateLabels
    ) {

        // ==========================
        // RESET BUILDER (MANDATORY)
        // ==========================
        builder.reset();

        // ==========================
        // HARD GUARDS (FAIL FAST)
        // ==========================
        if (workspace == null) {
            throw new IllegalStateException("Director: workspace is NULL");
        }
        if (tasks == null) {
            throw new IllegalStateException("Director: tasks list is NULL");
        }
        if (members == null) {
            throw new IllegalStateException("Director: members list is NULL");
        }
        if (progress == null) {
            throw new IllegalStateException("Director: progress is NULL");
        }

        // ==========================
        // DEBUG LOGS (VERY IMPORTANT)
        // ==========================
        System.out.println("\n=== BUILDING DASHBOARD ===");
        System.out.println("Workspace ID   : " + workspace.getTeamId());
        System.out.println("Workspace Name : " + workspace.getName());
        System.out.println("Tasks count    : " + tasks.size());
        System.out.println("Members count  : " + members.size());
        System.out.println("Leader         : " +
                (leader != null
                        ? leader.getFirstName() + " " + leader.getLastName()
                        : "NONE"));
        System.out.println("Progress       : " +
                progress.getDone() + "/" + progress.getTotal());
        System.out.println("Timeline start : " +
                (startPcts != null ? startPcts.size() : "NULL"));
        System.out.println("Timeline end   : " +
                (endPcts != null ? endPcts.size() : "NULL"));
        System.out.println("Date labels    : " +
                (dateLabels != null ? dateLabels.size() : "NULL"));
        System.out.println("==========================\n");

        // ==========================
        // BUILD STEP BY STEP
        // ==========================
        builder.setWorkspace(workspace);
        builder.setTasks(tasks);
        builder.setMembers(members);
        builder.setLeader(leader);
        builder.setComposite(composite);
        builder.setProgress(progress);
        builder.setStartPcts(startPcts);
        builder.setEndPcts(endPcts);
        builder.setDateLabels(dateLabels);

        // ==========================
        // FINAL BUILD
        // ==========================
        return builder.build();
    }
}
