package dz.usthb.eclipseworkspace.todo.observer;

public class TodoListObserver implements TaskObserver {
    @Override
    public void onTaskUpdated() {
        System.out.println("🔄 Todo list updated");
    }
}
