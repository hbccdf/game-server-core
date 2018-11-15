package server.core.di;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class AbstractGuiceInjector implements IInjector {
    private final Injector injector;

    public AbstractGuiceInjector() {
        injector = Guice.createInjector(newBinders());
    }

    @Override
    public <T> T getInstance(Class<T> clz) {
        return injector.getInstance(clz);
    }

    protected abstract AbstractModule newBinder();

    protected AbstractModule[] newBinders() {
        return new AbstractModule[]{newBinder()};
    }

    @Override
    public void regInstance(Class<?> clz, Object object) {
    }
}
