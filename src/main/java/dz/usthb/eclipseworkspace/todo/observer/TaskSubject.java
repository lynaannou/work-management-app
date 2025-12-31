package dz.usthb.eclipseworkspace.todo.observer;

public interface TaskSubject {
    void addObserver(TaskObserver o);
    void notifyObservers();
}
