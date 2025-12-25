package dz.usthb.eclipseworkspace.workspace.controller;

import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;

public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    public WorkspaceDashboard loadDashboard(int workspaceId) {
        try {
            return workspaceService.getDashboard(workspaceId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String loadDashboardAsJson(int workspaceId) {
        WorkspaceDashboard dashboard;

        try {
            dashboard = workspaceService.getDashboard(workspaceId);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Failed to load workspace\"}";
        }

        if (dashboard == null) {
            return "{\"error\":\"Workspace not found\"}";
        }

        return WorkspaceJsonSerializer.toJson(dashboard);
    }
}
