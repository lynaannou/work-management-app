public class Workspace {
    private String name;
    private int team_id;
    private List<TaskProgress> tasks;
    private Date created_at;
    private int open_tasks_count;
    private int done_tasks_count;
    public Workspace(String name, int team_id, List<IntStringPair> tasks, Date created_at, int open_tasks_count, int done_tasks_count) {
        this.name = name;
        this.team_id = team_id;
        this.tasks = tasks;
        this.created_at = created_at;
        this.open_tasks_count = open_tasks_count;
        this.done_tasks_count = done_tasks_count;


    }
    public String getName() {
        return name;
    }
    public int getTeam_id() {
        return team_id;
    }
    public Date getDate() {
        return created_at;
    }
    public List<IntStringPair> getTasks() {
        return tasks;
    }
    public Date getOpen_tasks_count() {
        return open_tasks_count;
    }
    public Date getDone_tasks_count() {
        return done_tasks_count;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }
    public void setTasks(List<IntStringPair> tasks) {
        this.tasks = tasks;
    }
    public void setCreated_at() {
        this.created_at = created_at;
           }

    public void setOpen_tasks_count() {
        this.open_tasks_count = open_tasks_count;
    }
    public void setDone_tasks_count() {
        this.done_tasks_count = done_tasks_count;
    }
}
