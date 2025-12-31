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
        System.out.println("🟦 [TaskService] CONSTRUCTOR called");
        this.taskDao = taskDao;
        this.teamMemberDao = new TeamMemberDaoJdbc();
    }

    // =================================================
    // CREATE
    // =================================================
    public void createTask(Task task) {

        System.out.println("🟥🟥🟥 [TaskService] createTask ENTER");
        System.out.println("➡️ teamId       = " + task.getTeamId());
        System.out.println("➡️ assigneeId   = " + task.getAssigneeId());
        System.out.println("➡️ title        = " + task.getTitle());
        System.out.println("➡️ status       = " + task.getStatus());
        System.out.println("➡️ progressPct  = " + task.getProgressPct());

        // ✅ Validate assignee belongs to team
        if (task.getAssigneeId() != null && task.getAssigneeId() > 0) {
            try {
                System.out.println("🟨 [TaskService] validating assignee belongs to team");

                boolean valid = teamMemberDao.belongsToTeam(
                        (long) task.getAssigneeId(),
                        (long) task.getTeamId()
                );

                System.out.println("🟨 [TaskService] belongsToTeam result = " + valid);

                if (!valid) {
                    System.err.println("❌ [TaskService] assignee DOES NOT belong to team");
                    throw new IllegalArgumentException(
                            "Assignee does not belong to this team"
                    );
                }

            } catch (Exception e) {
                System.err.println("❌ [TaskService] ERROR during team validation");
                e.printStackTrace();
                throw new RuntimeException(
                        "Error verifying team membership", e
                );
            }
        }

        System.out.println("🟨 [TaskService] calling taskDao.create()");
        taskDao.create(task);

        System.out.println("🟩 [TaskService] createTask EXIT (task persisted)");
    }

    // =================================================
    // READ BY TEAM
    // =================================================
    public List<Task> getTasksByTeam(int teamId) {

        System.out.println("🟥🟥🟥 [TaskService] getTasksByTeam ENTER");
        System.out.println("➡️ teamId = " + teamId);

        List<Task> tasks = taskDao.findByTeam(teamId);

        System.out.println("🟩 [TaskService] getTasksByTeam EXIT");
        System.out.println("➡️ tasks.size = " + tasks.size());

        return tasks;
    }

    // =================================================
    // STATUS ONLY
    // =================================================
    public void changeStatus(int taskId, String action) {

        System.out.println("🟥🟥🟥 [TaskService] changeStatus ENTER");
        System.out.println("➡️ taskId = " + taskId);
        System.out.println("➡️ action = " + action);

        Task task = taskDao.findById(taskId);

        if (task == null) {
            System.err.println("❌ [TaskService] changeStatus FAILED — task NOT FOUND");
            throw new IllegalArgumentException("Tâche introuvable");
        }

        System.out.println("🟨 [TaskService] task FOUND, current status = " + task.getStatus());

        switch (action.toUpperCase()) {
            case "START" -> {
                System.out.println("➡️ updating status to IN_PROGRESS");
                taskDao.updateStatusOnly(taskId, "IN_PROGRESS");
            }
            case "COMPLETE" -> {
                System.out.println("➡️ updating status to DONE");
                taskDao.updateStatusOnly(taskId, "DONE");
            }
            case "CANCEL" -> {
                System.out.println("➡️ updating status to CANCELLED");
                taskDao.updateStatusOnly(taskId, "CANCELLED");
            }
            default -> {
                System.err.println("❌ [TaskService] UNKNOWN ACTION");
                throw new IllegalArgumentException("Action inconnue : " + action);
            }
        }

        System.out.println("🟩 [TaskService] changeStatus EXIT");
    }

    // =================================================
    // TITLE ONLY
    // =================================================
    public void updateTitle(int taskId, String title) {

        System.out.println("🟥🟥🟥 [TaskService] updateTitle ENTER");
        System.out.println("➡️ taskId = " + taskId);
        System.out.println("➡️ title  = " + title);

        if (title == null || title.isBlank()) {
            System.err.println("❌ [TaskService] INVALID TITLE");
            throw new IllegalArgumentException("Titre invalide");
        }

        taskDao.updateTitleOnly(taskId, title);

        System.out.println("🟩 [TaskService] updateTitle EXIT");
    }

    // =================================================
    // READ BY ID
    // =================================================
    public Task getTaskById(int taskId) {

        System.out.println("🟥🟥🟥 [TaskService] getTaskById ENTER");
        System.out.println("➡️ taskId = " + taskId);

        Task task = taskDao.findById(taskId);

        if (task == null) {
            System.err.println("❌ [TaskService] getTaskById FAILED — task NOT FOUND");
            throw new IllegalArgumentException("Tâche introuvable");
        }

        System.out.println("🟩 [TaskService] getTaskById EXIT — task FOUND");
        return task;
    }

    // =================================================
    // DESCRIPTION ONLY
    // =================================================
    public void updateDescription(int taskId, String description) {

        System.out.println("🟥🟥🟥 [TaskService] updateDescription ENTER");
        System.out.println("➡️ taskId = " + taskId);

        taskDao.updateDescriptionOnly(taskId, description);

        System.out.println("🟩 [TaskService] updateDescription EXIT");
    }

    // =================================================
    // DUE DATE ONLY
    // =================================================
    public void updateDueDate(int taskId, LocalDate dueDate) {

        System.out.println("🟥🟥🟥 [TaskService] updateDueDate ENTER");
        System.out.println("➡️ taskId = " + taskId);
        System.out.println("➡️ dueDate = " + dueDate);

        if (dueDate == null) {
            System.err.println("❌ [TaskService] NULL dueDate");
            throw new IllegalArgumentException("Date invalide");
        }

        taskDao.updateDueDateOnly(taskId, dueDate);

        System.out.println("🟩 [TaskService] updateDueDate EXIT");
    }

    // =================================================
    // FULL UPDATE
    // =================================================
    public void updateTask(Task task) {

        System.out.println("🟥🟥🟥 [TaskService] updateTask ENTER");
        System.out.println("➡️ taskId = " + task.getId());

        Task existing = taskDao.findById(task.getId());

        if (existing == null) {
            System.err.println("❌ [TaskService] updateTask FAILED — task NOT FOUND");
            throw new IllegalArgumentException("Tâche introuvable");
        }

        taskDao.update(task);

        System.out.println("🟩 [TaskService] updateTask EXIT");
    }

    // =================================================
    // DELETE
    // =================================================
    public void deleteTask(int taskId) {

        System.out.println("🟥🟥🟥 [TaskService] deleteTask ENTER");
        System.out.println("➡️ taskId = " + taskId);

        Task task = taskDao.findById(taskId);

        if (task == null) {
            System.err.println("❌ [TaskService] deleteTask FAILED — task NOT FOUND");
            throw new IllegalArgumentException("Tâche introuvable");
        }

        System.out.println("🟨 [TaskService] task FOUND — proceeding to DAO delete");
        System.out.println("➡️ teamId = " + task.getTeamId());
        System.out.println("➡️ status = " + task.getStatus());

        taskDao.delete(taskId);

        System.out.println("🟩 [TaskService] deleteTask EXIT");
    }

    // =================================================
    // TEAM MEMBERS
    // =================================================
    public List<TeamMember> getTeamMembers(int teamId) {

        System.out.println("🟥🟥🟥 [TaskService] getTeamMembers ENTER");
        System.out.println("➡️ teamId = " + teamId);

        try {
            List<TeamMember> members = teamMemberDao.findByTeamId((long) teamId);
            System.out.println("🟩 [TaskService] getTeamMembers EXIT — count=" + members.size());
            return members;
        } catch (Exception e) {
            System.err.println("❌ [TaskService] getTeamMembers FAILED");
            e.printStackTrace();
            throw new RuntimeException("Failed to load team members", e);
        }
    }
}
