public class Workspace {
    private String name;
    private int team_id;
    private List<TaskProgress> tasks;
    public Workspace(String name, int team_id, List<IntStringPair> tasks) {
        this.name = name;
        this.team_id = team_id;
        this.tasks = tasks;

    }
    public String getName() {
        return name;
    }
    public int getTeam_id() {
        return team_id;
    }
    public List<IntStringPair> getTasks() {
        return tasks;
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
}
