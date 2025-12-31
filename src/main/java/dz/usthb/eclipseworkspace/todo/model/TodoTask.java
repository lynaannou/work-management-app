package dz.usthb.eclipseworkspace.todo.model;

import java.sql.Date;

public class TodoTask {

    private int itemId;
    private String title;
    private String description;
    private Date dueDate;
    private String status;

    public TodoTask(int itemId, String title, String description, Date dueDate, String status) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getItemId() { return itemId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getDueDate() { return dueDate; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }
}
