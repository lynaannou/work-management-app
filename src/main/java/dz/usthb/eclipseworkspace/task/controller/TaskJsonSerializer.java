package dz.usthb.eclipseworkspace.task.controller;

import dz.usthb.eclipseworkspace.task.model.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TaskJsonSerializer {

    // =================================================
    // TO JSON (UNCHANGED)
    // =================================================
    public static String toJson(List<Task> tasks) {

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);

            sb.append("{")
              .append("\"id\":").append(t.getId()).append(",")
              .append("\"title\":\"").append(escape(t.getTitle())).append("\",")

              .append("\"description\":")
              .append(t.getDescription() != null
                      ? "\"" + escape(t.getDescription()) + "\""
                      : "null")
              .append(",")

              .append("\"status\":\"").append(t.getStatus()).append("\",")
              .append("\"progress\":").append(t.getProgressPct()).append(",")

              .append("\"startDate\":")
              .append(t.getStartDate() != null
                      ? "\"" + t.getStartDate() + "\""
                      : "null")
              .append(",")

              .append("\"dueDate\":")
              .append(t.getDueDate() != null
                      ? "\"" + t.getDueDate() + "\""
                      : "null")
              .append(",")

              .append("\"assigneeId\":").append(t.getAssigneeId()).append(",")

              .append("\"assigneeName\":")
              .append(t.getAssigneeName() != null
                      ? "\"" + escape(t.getAssigneeName()) + "\""
                      : "null")

              .append("}");

            if (i < tasks.size() - 1) sb.append(",");
        }

        sb.append("]");
        return sb.toString();
    }

    // =================================================
    // 🔥 FROM JSON (NEW)
    // =================================================
    public static Task fromJson(Map<String, Object> data) {

        Task task = new Task();

        task.setTeamId(((Number) data.get("teamId")).intValue());
        task.setTitle((String) data.get("title"));
        task.setDescription((String) data.get("description"));

        Object assignee = data.get("assigneeId");
        if (assignee != null) {
            task.setAssigneeId(((Number) assignee).intValue());
        }

        Object dueDate = data.get("dueDate");
        if (dueDate != null && !dueDate.toString().isBlank()) {
            task.setDueDate(LocalDate.parse(dueDate.toString()));
        }

        // 🔒 FORCE BUSINESS RULES
        task.setStateFromString("TODO");
        task.setStartDate(LocalDate.now());
        task.setProgressPct(0);

        return task;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
