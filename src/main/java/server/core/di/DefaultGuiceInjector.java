package server.core.di;

import com.google.inject.AbstractModule;

public class DefaultGuiceInjector extends GuiceInjector{
    public static final IInjector INSTANCE = new DefaultGuiceInjector();

    private DefaultGuiceInjector() {

    }

    @Override
    protected AbstractModule newBinder() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                // do nothing
            }
        };
    }
}
