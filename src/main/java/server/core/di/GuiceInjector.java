package server.core.di;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class GuiceInjector implements IInjector {
    private Injector injector;
    public GuiceInjector() {
        injector = Guice.createInjector(newBinder());
    }

    @Override
    public <T> T getInstance(Class<T> clz) {
        return injector.getInstance(clz);
    }

    protected abstract AbstractModule newBinder();
}
