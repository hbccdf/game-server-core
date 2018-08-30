package server.core.ecs;

public interface IEntity extends Iterable<IComponent> {
    IComponent parent();

    void update(long now);

    <T extends IComponent> T getComponent(Class<T> componentType);
    <T extends IComponent> T addComponent(Class<T> componentType);
    <T extends IComponent> T removeComponent(Class<T> componentType);
}
