package server.core.ecs;

import com.sun.org.apache.bcel.internal.generic.ICONST;
import server.core.util.ClassReflection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public abstract class BaseEntity implements IEntity {
    public static final String PARENT_FIELD_NAME = "parent";

    private IComponent parent;

    private HashMap<Class<?>, IComponent> components = new LinkedHashMap<>();


    @Override
    public IComponent parent() {
        return parent;
    }

    @Override
    public void update(long now) {
        Iterator<IComponent> it = components.values().iterator();
        while (it.hasNext()) {
            IComponent next = it.next();
            if (next != null) {
                next.update(now);
            }
        }
    }

    @Override
    public <T extends IComponent> T getComponent(Class<T> componentType) {
        return (T)components.get(componentType);
    }

    @Override
    public <T extends IComponent> T addComponent(Class<T> componentType) {
        T com = newComponent(componentType);
        ClassReflection.set(com, BaseComponent.ENTITY_FIELD_NAME, this);
        components.put(componentType, com);
        return com;
    }

    @Override
    public <T extends IComponent> T removeComponent(Class<T> componentType) {
        return (T)components.remove(componentType);
    }

    @Override
    public Iterator<IComponent> iterator() {
        return components.values().iterator();
    }

    protected abstract <T extends IComponent> T newComponent(Class<T> componentType);

    public void enable(boolean enable) {
        Iterator<IComponent> it = iterator();
        while (it.hasNext()) {
            IComponent comp = it.next();
            if (comp != null) {
                if (enable) {
                    comp.start();
                } else {
                    comp.stop();
                }
            }
        }
    }
}
