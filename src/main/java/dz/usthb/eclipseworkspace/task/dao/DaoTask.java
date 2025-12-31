package dz.usthb.eclipseworkspace.task.dao;

import java.time.LocalDate;
import java.util.List;

import dz.usthb.eclipseworkspace.task.model.Task;

public interface DaoTask {

    // ===============================
    // BASIC CRUD
    // ===============================
    void create(Task task);

    void update(Task task);

    void delete(int taskId);

    Task findById(int taskId);

    List<Task> findByTeam(int teamId);

    // ===============================
    // ✅ PARTIAL UPDATES (SAFE)
    // ===============================

    /** Update ONLY the task status */
    void updateStatusOnly(int taskId, String status);

    /** Update ONLY the task title */
    void updateTitleOnly(int taskId, String title);

    /** Update ONLY the task description */
    void updateDescriptionOnly(int taskId, String description);

    /** Update ONLY the due date */
    void updateDueDateOnly(int taskId, LocalDate dueDate);
    // ===============================
// DELETE BY TEAM (FOR WORKSPACE DELETE)
// ===============================
void deleteByTeam(int teamId);

}
