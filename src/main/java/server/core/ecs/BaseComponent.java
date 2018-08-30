package server.core.ecs;

import server.core.util.ClassReflection;

public class BaseComponent implements IComponent {
    public static final String ENTITY_FIELD_NAME = "entity";
    private IEntity entity;

    @Override
    public final IEntity parent() {
        return entity;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void update(long now) {

    }

    @Override
    public <T extends IComponent> T getComponent(Class<T> componentType) {
        return entity.getComponent(componentType);
    }

    @Override
    public <T extends IComponent> T addComponent(Class<T> componentType) {
        return entity.addComponent(componentType);
    }

    @Override
    public <T extends IComponent> T removeComponent(Class<T> componentType) {
        return entity.removeComponent(componentType);
    }

    @Override
    public void child(IEntity entity) {
        ClassReflection.set(entity, BaseEntity.PARENT_FIELD_NAME, this);
    }
}
