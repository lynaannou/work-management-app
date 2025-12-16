package dz.usthb.eclipseworkspace.workspace.controller;

import dz.usthb.eclipseworkspace.workspace.model.Task;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.workspace.service.builder.WorkspaceDashboard;

public class WorkspaceJsonSerializer {

    public static String toJson(WorkspaceDashboard d) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");


        sb.append("\"workspace\":{")
          .append("\"name\":\"").append(d.getWorkspace().getName()).append("\",")
          .append("\"team_id\":").append(d.getWorkspace().getTeam_id())
          .append("},");


        AppUser leader = d.getLeader();
        sb.append("\"leader\":{")
          .append("\"firstName\":\"").append(leader.getFirstName()).append("\",")
          .append("\"lastName\":\"").append(leader.getLastName()).append("\"")
          .append("},");


        sb.append("\"tasks\":[");
        for (int i = 0; i < d.getTasks().size(); i++) {
            Task t = d.getTasks().get(i);

            float startPct = d.getStartPcts().get(i);
            float endPct   = d.getEndPcts().get(i);

            sb.append("{")
              .append("\"id\":").append(t.getTask_id()).append(",")
              .append("\"title\":\"").append(t.getTitle()).append("\",")
              .append("\"status\":\"").append(t.getStatus()).append("\",")
              .append("\"startPct\":").append(startPct).append(",")
              .append("\"endPct\":").append(endPct)
              .append("}");

            if (i < d.getTasks().size() - 1) sb.append(",");
        }
        sb.append("],");


        sb.append("\"members\":[");
        for (int i = 0; i < d.getMembers().size(); i++) {
            AppUser u = d.getMembers().get(i);

            sb.append("{")
              .append("\"id\":").append(u.getUser_id()).append(",")
              .append("\"name\":\"").append(u.getFirstName()).append(" ").append(u.getLastName()).append("\"")
              .append("}");

            if (i < d.getMembers().size() - 1) sb.append(",");
        }
        sb.append("],");


        sb.append("\"dateLabels\":[");
        for (int i = 0; i < d.getDateLabels().size(); i++) {
            sb.append("\"").append(d.getDateLabels().get(i)).append("\"");
            if (i < d.getDateLabels().size() - 1) sb.append(",");
        }
        sb.append("],");


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
}
