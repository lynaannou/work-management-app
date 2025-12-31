package dz.usthb.eclipseworkspace.workspace.controller;

import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;

import java.util.List;

public class WorkspaceJsonSerializer {

    public static String toJson(WorkspaceDashboard d) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // =========================
        // WORKSPACE
        // =========================
        sb.append("\"workspace\":{")
          .append("\"teamId\":").append(d.getWorkspace().getTeamId()).append(",")
          .append("\"name\":\"").append(escape(d.getWorkspace().getName())).append("\",")
          .append("\"description\":\"")
          .append(escape(d.getWorkspace().getDescription()))
          .append("\"")
          .append("},");


        // =========================
        // 👤 CURRENT USER (NAVBAR)
        // =========================
        AppUser u = d.getCurrentUser();
        if (u != null) {
            sb.append("\"currentUser\":{")
              .append("\"firstName\":\"").append(escape(u.getFirstName())).append("\",")
              .append("\"lastName\":\"").append(escape(u.getLastName())).append("\"")
              .append("},");
        } else {
            sb.append("\"currentUser\":null,");
        }

        // =========================
        // 🔑 CURRENT USER ROLE
        // =========================
        sb.append("\"currentUserRole\":\"")
          .append(escape(d.getCurrentUserRole()))
          .append("\",");

        // =========================
        // TASKS
        // =========================
        sb.append("\"tasks\":[");

        List<Task> tasks = d.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("{")
              .append("\"id\":").append(t.getId()).append(",")
              .append("\"title\":\"").append(escape(t.getTitle())).append("\",")
              .append("\"status\":\"").append(t.getStatus()).append("\"")
              .append("}");
            if (i < tasks.size() - 1) sb.append(",");
        }
        sb.append("],");

        // =========================
        // TIMELINE
        // =========================
        sb.append("\"startPcts\":").append(toJsonArray(d.getStartPcts())).append(",");
        sb.append("\"endPcts\":").append(toJsonArray(d.getEndPcts())).append(",");
        sb.append("\"dateLabels\":").append(toJsonArray(d.getDateLabels())).append(",");

        // =========================
        // PROGRESS
        // =========================
        sb.append("\"progress\":{")
          .append("\"todo\":").append(d.getProgress().getTodo()).append(",")
          .append("\"inProgress\":").append(d.getProgress().getInProgress()).append(",")
          .append("\"done\":").append(d.getProgress().getDone()).append(",")
          .append("\"total\":").append(d.getProgress().getTotal()).append(",")
          .append("\"percent\":").append(d.getProgress().getPercent())
          .append("}");

        sb.append("}");
        return sb.toString();
    }

    private static String toJsonArray(List<?> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (v instanceof Number) sb.append(v);
            else sb.append("\"").append(escape(v.toString())).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
