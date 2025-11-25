public class TaskProgress {
    private String taskName;
    private int percentageStart;
    private int percentageEnd;
    private String status;
    private Date startDate;
    private Date endDate;
    public TaskProgress(String taskName, int percentageStart, int percentageEnd, String status, Date startDate, Date endDate) {
        this.taskName = taskName;
        this.percentageStart = percentageStart;
        this.percentageEnd = percentageEnd;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public String getTaskName() {
        return taskName;
    }
    public int getPercentageStart() {
        return percentageStart;
    }
    public int getPercentageEnd() {
        return percentageEnd;
    }
    public String getStatus() {
        return status;
    }
    public Date getStartDate() {
        return startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    public void setPercentageStart(int percentageStart) {
        this.percentageStart = percentageStart;
    }
    public void setPercentageEnd(int percentageEnd) {
        this.percentageEnd = percentageEnd;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
