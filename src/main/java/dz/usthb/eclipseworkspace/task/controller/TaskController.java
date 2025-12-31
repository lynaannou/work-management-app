package dz.usthb.eclipseworkspace.task.controller;

import com.google.gson.Gson;
import dz.usthb.eclipseworkspace.common.json.GsonProvider;
import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.task.service.TaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TaskController {

    private final TaskService taskService;

    // ✅ DEPENDENCY INJECTION
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ==========================
    // CREATE TASK (FORM BASED)
    // ==========================
    public boolean createTask(
            String title,
            String description,
            int teamId,
            String dueDate,
            Integer assignedMemberId
    ) {
        System.out.println("🟨 [TaskController] createTask()");
        System.out.println("   teamId=" + teamId);

        try {
            Task task = new Task(title, description, teamId);

            // 🔒 business defaults
            task.setStateFromString("TODO");
            task.setStartDate(LocalDate.now());
            task.setProgressPct(0);

            if (dueDate != null && !dueDate.isBlank()) {
                task.setDueDate(LocalDate.parse(dueDate));
            }

            if (assignedMemberId != null && assignedMemberId > 0) {
                task.setAssigneeId(assignedMemberId);
            }

            taskService.createTask(task);
            System.out.println("🟨 [TaskController] task created successfully");
            return true;

        } catch (Exception e) {
            System.err.println("❌ [TaskController] createTask failed");
            e.printStackTrace();
            return false;
        }
    }

    // ==========================
    // CREATE TASK (JSON – JS)
    // ==========================
    public void createTaskFromJson(String json) {

        System.out.println("🟨 [TaskController] createTaskFromJson");

        // Parse JSON → Map
        Map<String, Object> data =
                GsonProvider.get().fromJson(json, Map.class);

        Task task = TaskJsonSerializer.fromJson(data);

        // 🔒 FORCE BUSINESS RULES
        task.setStateFromString("TODO");
        task.setStartDate(LocalDate.now());
        task.setProgressPct(0);

        taskService.createTask(task);
    }

    // ==========================
    // LOAD TASKS (RAW)
    // ==========================
    public List<Task> loadTasks(int teamId) {
        System.out.println("🟨 [TaskController] loadTasks teamId=" + teamId);

        try {
            List<Task> tasks = taskService.getTasksByTeam(teamId);
            System.out.println("🟨 [TaskController] tasks loaded: " + tasks.size());
            return tasks;

        } catch (Exception e) {
            System.err.println("❌ [TaskController] loadTasks failed");
            e.printStackTrace();
            return List.of();
        }
    }

    // ==========================
    // LOAD TASKS AS JSON
    // ==========================
    public String loadTasksAsJson(int teamId) {
        System.out.println("🟨 [TaskController] loadTasksAsJson teamId=" + teamId);

        List<Task> tasks = loadTasks(teamId);
        String json = TaskJsonSerializer.toJson(tasks);

        System.out.println("🟦 [TaskController] JSON length=" + json.length());
        return json;
    }

    // ==========================
    // DELETE TASK
    // ==========================
    public boolean deleteTaskById(int taskId) {
    try {
        taskService.deleteTask(taskId);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
    }


    // ==========================
    // CHANGE TASK STATUS
    // ==========================
    public boolean changeStatus(int taskId, String action) {
        try {
            taskService.changeStatus(taskId, action);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // EDIT TASK (FULL FORM SAVE)
    // =====================================================
    public boolean updateTask(
            int taskId,
            String title,
            String description,
            String dueDate,
            String status
    ) {
        try {
            Task task = taskService.getTaskById(taskId);

            if (title != null && !title.isBlank()) {
                task.setTitle(title);
            }

            if (description != null) {
                task.setDescription(description);
            }

            if (dueDate != null && !dueDate.isBlank()) {
                task.setDueDate(LocalDate.parse(dueDate));
            }

            if (status != null && !status.isBlank()) {
                task.setStateFromString(status);
            }

            taskService.updateTask(task);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // TITLE ONLY
    // =====================================================
    public boolean updateTitle(int taskId, String title) {
        try {
            taskService.updateTitle(taskId, title);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public String loadTeamMembersAsJson(int teamId) {
    try {
        var members = taskService.getTeamMembers(teamId);
        return GsonProvider.get().toJson(members);
    } catch (Exception e) {
        e.printStackTrace();
        return "[]";
    }
}


    // =====================================================
    // DESCRIPTION ONLY
    // =====================================================
    public boolean updateDescription(int taskId, String description) {
        try {
            taskService.updateDescription(taskId, description);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // DUE DATE ONLY
    // =====================================================
    public boolean updateDueDate(int taskId, String dueDate) {
        try {
            taskService.updateDueDate(
                    taskId,
                    LocalDate.parse(dueDate)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
