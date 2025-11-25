public class DaoWorkspace extends Dao<Workspace> {
    private List<Workspace> workspaces = new ArrayList<>();

    public DaoWorkspace() {
        workspaces.add(new Workspace("project test", 4, "15/11/2025", 0, 0, 0));
    }
    @Override
    public Optional<Workspace> get (int team_id) {
        return Optional.ofNullables(workspaces.get(team_id));

    }
    @Override
    public List<Workspace> getAll() {
        return workspaces;

    }
    @Override
    public void save(Workspace workspace) {
        workspaces.add(workspace);

    }
    @Override
    public void update(Workspace workspace, String[] params) {
        workspaces.setName(Objects.requireNotNull(
            params[0], "Name cannot be null"
        ));
        workspaces.add(workspace);
    }
    @Override
    public void delete(Workspace workspace) {
        workspaces.remove(workspace);
    }
    
}
