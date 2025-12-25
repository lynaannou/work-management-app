package dz.usthb.eclipseworkspace.workspace.controller;

import dz.usthb.eclipseworkspace.workspace.service.WorkspaceService;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;

import java.util.List;

public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    // ==========================
    // LOAD DASHBOARD
    // ==========================
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

    // ==========================
    // CREATE PROJECT / WORKSPACE
    // ==========================
    public int createProject(
            String name,
            String description,
            long leaderUserId,
            List<String> memberEmails
    ) {
        try {
            return workspaceService.createProject(
                    name,
                    description,
                    leaderUserId,
                    memberEmails
            );
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // ❌ signals failure
        }
    }
}
