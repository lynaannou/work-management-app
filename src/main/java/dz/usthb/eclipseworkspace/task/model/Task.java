package dz.usthb.eclipseworkspace.task.model;

import java.time.LocalDate;

import com.google.gson.annotations.SerializedName;

import dz.usthb.eclipseworkspace.task.model.state.TaskState;
import dz.usthb.eclipseworkspace.task.model.state.TodoState;
import dz.usthb.eclipseworkspace.task.model.state.InProgressState;
import dz.usthb.eclipseworkspace.task.model.state.DoneState;
import dz.usthb.eclipseworkspace.task.model.state.CancelledState;


public class Task {

    /* ===================== DB FIELDS ===================== */

    @SerializedName("id")
    private int id;                     // task_id (PK)
    private int teamId;                 // team_id (FK)
    private Integer assigneeId;          // team_member_id (FK, nullable)

    private String title;
    private String description;
    private String status;               // TODO, IN_PROGRESS, DONE, CANCELLED
    private int progressPct;

    /* ===================== INTERNAL (NOT JSON) ===================== */

    private transient LocalDate startDate;
    private transient LocalDate dueDate;
    private transient LocalDate createdAt;
    private transient LocalDate completedAt;

    private transient TaskState currentState;

    /* ===================== DISPLAY DATA ===================== */

    private String assigneeFirstName;
    private String assigneeLastName;
    
    /* ===================== CONSTRUCTORS ===================== */

    public Task() {
        this.progressPct = 0;
        setState(new TodoState());
    }

    public Task(String title, String description, int teamId) {
        this.title = title;
        this.description = description;
        this.teamId = teamId;
        this.progressPct = 0;
        setState(new TodoState());
    }

    /* ===================== STATE HANDLING ===================== */

    public void start() {
        currentState.start(this);
        this.startDate = LocalDate.now();
        syncState();
    }

    public void complete() {
        currentState.complete(this);
        this.completedAt = LocalDate.now();
        syncState();
    }

    public void cancel() {
        currentState.cancel(this);
        syncState();
    }

    private void syncState() {
        switch (status) {
            case "IN_PROGRESS" -> currentState = new InProgressState();
            case "DONE"        -> currentState = new DoneState();
            case "CANCELLED"   -> currentState = new CancelledState();
            default            -> currentState = new TodoState();
        }
    }

    public void setState(TaskState state) {
        this.currentState = state;
        this.status = state.getName();
    }

    /* ===================== GETTERS & SETTERS ===================== */

    // ✅ ID (NOW EXPLICIT + SAFE)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    // ✅ Nullable FK (CRITICAL FIX)
    public Integer getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public int getProgressPct() {
        return progressPct;
    }

    public void setProgressPct(int progressPct) {
        this.progressPct = progressPct;
    }

    /* ===================== INTERNAL LocalDate ===================== */

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }

    /* ===================== JSON-SAFE GETTERS ===================== */
    // 👇 THESE are serialized by Gson

    public String getStartDateIso() {
        return startDate != null ? startDate.toString() : null;
    }

    @SerializedName("dueDate")
    public String getDueDateIso() {
        return dueDate != null ? dueDate.toString() : null;
    }

    

    public String getCreatedAtIso() {
        return createdAt != null ? createdAt.toString() : null;
    }
    /* ===================== STATUS FROM STRING ===================== */
public void setStateFromString(String status) {
    if (status == null) {
        setState(new TodoState());
        return;
    }

    switch (status) {
        case "IN_PROGRESS" -> setState(new InProgressState());
        case "DONE"        -> setState(new DoneState());
        case "CANCELLED"   -> setState(new CancelledState());
        default            -> setState(new TodoState());
    }
}


    public String getCompletedAtIso() {
        return completedAt != null ? completedAt.toString() : null;
    }


    /* ===================== ASSIGNEE DISPLAY ===================== */

    public String getAssigneeName() {
        if (assigneeFirstName == null) return "—";
        return assigneeFirstName + " " + assigneeLastName;
    }

    public void setAssigneeFirstName(String firstName) {
        this.assigneeFirstName = firstName;
    }

    public void setAssigneeLastName(String lastName) {
        this.assigneeLastName = lastName;
    }
    
}
