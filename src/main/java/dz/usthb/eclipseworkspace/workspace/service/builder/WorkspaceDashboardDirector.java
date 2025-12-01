package dz.usthb.eclipseworkspace.workspace.service.builder;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.workspace.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.Workspace;
import dz.usthb.eclipseworkspace.workspace.service.components.WorkspaceComposite;
import dz.usthb.eclipseworkspace.workspace.service.components.TaskProgress;

import java.util.List;

public class WorkspaceDashboardDirector {

    public WorkspaceDashboard buildDashboard(
            WorkspaceDashboardBuilder builder,
            Workspace ws,
            List<Task> tasks,
            List<AppUser> members,
            AppUser leader,
            WorkspaceComposite composite,
            TaskProgress progress,
            List<Float> startPcts,
            List<Float> endPcts,
            List<String> dateLabels
    ) {
        builder.reset();

        builder.setWorkspace(ws);
        builder.setTasks(tasks);
        builder.setMembers(members);
        builder.setLeader(leader);
        builder.setComposite(composite);
        builder.setProgress(progress);

        builder.setStartPcts(startPcts);
        builder.setEndPcts(endPcts);
        builder.setDateLabels(dateLabels);

        return builder.build();
    }
}
