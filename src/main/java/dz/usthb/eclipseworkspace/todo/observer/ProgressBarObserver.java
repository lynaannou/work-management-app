package dz.usthb.eclipseworkspace.todo.observer;

public class ProgressBarObserver implements TaskObserver {
    @Override
    public void onTaskUpdated() {
        System.out.println("📊 Progress bar updated");
    }
}
