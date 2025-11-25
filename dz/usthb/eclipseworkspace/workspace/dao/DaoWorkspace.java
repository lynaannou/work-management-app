public class DaoWorkspace extends Dao<Workspace> {
    private List<Workspace> workspaces = new ArrayList<>();

    public DaoWorkspace() {
        workspaces.add(new Workspace())
    }
    
}
