package server.core.ecs;

public interface IComponent {
    IEntity parent();

    void start();
    void stop();

    void update(long now);

    <T extends IComponent> T getComponent(Class<T> componentType);

    <T extends IComponent> T addComponent(Class<T> componentType);

    <T extends IComponent> T removeComponent(Class<T> componentType);

    void child(IEntity entity);
}
