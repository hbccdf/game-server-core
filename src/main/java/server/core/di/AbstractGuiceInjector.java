package server.core.di;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.HashMap;

public abstract class AbstractGuiceInjector implements IInjector {
    private Injector injector;

    private HashMap<Class<?>, Object> instances = new HashMap<>();

    public AbstractGuiceInjector() {
        injector = Guice.createInjector(newBinders());
    }

    @Override
    public <T> T getInstance(Class<T> clz) {
//        Object obj = instances.get(clz);
//        if (obj != null) {
//            return (T) obj;
//        }
        return injector.getInstance(clz);
    }

    protected abstract AbstractModule newBinder();

    protected AbstractModule[] newBinders() {
        return new AbstractModule[]{newBinder()};
    }

    @Override
    public void regInstance(Class<?> clz, Object object) {
        //instances.put(clz, object);
    }
}
