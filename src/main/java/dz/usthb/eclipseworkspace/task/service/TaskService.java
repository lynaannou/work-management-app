package dz.usthb.eclipseworkspace.task.service;

import dz.usthb.eclipseworkspace.task.dao.DaoTask;
import dz.usthb.eclipseworkspace.task.model.Task;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoJdbc;
import dz.usthb.eclipseworkspace.team.model.TeamMember;

import java.time.LocalDate;
import java.util.List;

public class TaskService {

    private final DaoTask taskDao;
    private final TeamMemberDao teamMemberDao;

    public TaskService(DaoTask taskDao) {
        this.taskDao = taskDao;
        this.teamMemberDao = new TeamMemberDaoJdbc();
    }

    // =================================================
    // CREATE
    // =================================================
public void createTask(Task task) {

    System.out.println("🟧 [TaskService] createTask()");
    System.out.println("   teamId=" + task.getTeamId());
    System.out.println("   assigneeId=" + task.getAssigneeId());

    // ✅ Validate assignee belongs to team
    if (task.getAssigneeId() != null && task.getAssigneeId() > 0) {
        try {
            boolean valid = teamMemberDao.belongsToTeam(
                (long) task.getAssigneeId(), // team_member_id
                (long) task.getTeamId()      // team_id
            );

            if (!valid) {
                throw new IllegalArgumentException(
                    "Assignee does not belong to this team"
                );
            }

        } catch (Exception e) {
            throw new RuntimeException(
                "Error verifying team membership", e
            );
        }
    }

    taskDao.create(task);
    System.out.println("🟧 [TaskService] task persisted");
}


    // =================================================
    // READ
    // =================================================
    public List<Task> getTasksByTeam(int teamId) {

        System.out.println("🟧 [TaskService] getTasksByTeam teamId=" + teamId);
        List<Task> tasks = taskDao.findByTeam(teamId);
        System.out.println("🟧 [TaskService] DAO returned " + tasks.size() + " tasks");

        return tasks;
    }

    // =================================================
    // ✅ STATUS ONLY
    // =================================================
    public void changeStatus(int taskId, String action) {

        Task task = taskDao.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Tâche introuvable");
        }

        switch (action.toUpperCase()) {
            case "START" -> taskDao.updateStatusOnly(taskId, "IN_PROGRESS");
            case "COMPLETE" -> taskDao.updateStatusOnly(taskId, "DONE");
            case "CANCEL" -> taskDao.updateStatusOnly(taskId, "CANCELLED");
            default -> throw new IllegalArgumentException("Action inconnue : " + action);
        }
    }

    // =================================================
    // ✅ TITLE ONLY
    // =================================================
    public void updateTitle(int taskId, String title) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Titre invalide");
        }

        taskDao.updateTitleOnly(taskId, title);
    }
    // =================================================
// READ BY ID
// =================================================
public Task getTaskById(int taskId) {

    System.out.println("🟧 [TaskService] getTaskById taskId=" + taskId);

    Task task = taskDao.findById(taskId);

    if (task == null) {
        throw new IllegalArgumentException("Tâche introuvable");
    }

    return task;
}

    // =================================================
    // ✅ DESCRIPTION ONLY
    // =================================================
    public void updateDescription(int taskId, String description) {

        taskDao.updateDescriptionOnly(taskId, description);
    }

    // =================================================
    // ✅ DUE DATE ONLY
    // =================================================
    public void updateDueDate(int taskId, LocalDate dueDate) {

        if (dueDate == null) {
            throw new IllegalArgumentException("Date invalide");
        }

        taskDao.updateDueDateOnly(taskId, dueDate);
    }

    // =================================================
    // FULL FORM SAVE (EDIT PAGE)
    // =================================================
    public void updateTask(Task task) {

        Task existing = taskDao.findById(task.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Tâche introuvable");
        }

        taskDao.update(task);
    }

    // =================================================
    // DELETE
    // =================================================
    public void deleteTask(int taskId) {

        Task task = taskDao.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Tâche introuvable");
        }

        taskDao.delete(taskId);
    }
    public List<TeamMember> getTeamMembers(int teamId) {
    try {
        return teamMemberDao.findByTeamId((long) teamId);
    } catch (Exception e) {
        throw new RuntimeException("Failed to load team members", e);
    }
}

}
