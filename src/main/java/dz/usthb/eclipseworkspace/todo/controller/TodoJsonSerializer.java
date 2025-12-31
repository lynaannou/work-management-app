package dz.usthb.eclipseworkspace.todo.controller;

import dz.usthb.eclipseworkspace.todo.model.TodoTask;
import java.util.List;

public class TodoJsonSerializer {

    public static String toJson(List<TodoTask> tasks, int progress) {

    StringBuilder sb = new StringBuilder();
    sb.append("{\"progress\":").append(progress).append(",\"tasks\":[");

    for (int i = 0; i < tasks.size(); i++) {
        TodoTask t = tasks.get(i);

        sb.append("{")
          .append("\"id\":").append(t.getItemId()).append(",")
          .append("\"title\":\"").append(t.getTitle()).append("\",")
          .append("\"description\":\"").append(t.getDescription()).append("\",")
          .append("\"due\":\"").append(t.getDueDate()).append("\",")
          .append("\"status\":\"").append(t.getStatus()).append("\"")
          .append("}");

        if (i < tasks.size() - 1) sb.append(",");
    }

    sb.append("]}");
    return sb.toString();
}

}
