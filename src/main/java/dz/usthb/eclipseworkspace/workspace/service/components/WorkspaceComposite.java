package dz.usthb.eclipseworkspace.workspace.service.components;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceComposite implements WorkspaceComponent {

    private List<WorkspaceComponent> components = new ArrayList<>();

    public void addComponent(WorkspaceComponent component) {
        components.add(component);
    }

    @Override
    public void display() {
        for (WorkspaceComponent component : components) {
            component.display();
        }
    }

    @Override
    public int getProgress() {
        if (components.isEmpty()) {
            return 0; // avoid division by zero
        }

        int total = 0;
        for (WorkspaceComponent c : components) {
            total += c.getProgress();
        }

        return total / components.size();  // average progress
    }

    public List<WorkspaceComponent> getComponents() {
        return components;
    }
}
