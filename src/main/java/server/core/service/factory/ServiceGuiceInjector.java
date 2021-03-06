package server.core.service.factory;

import com.google.inject.AbstractModule;
import server.core.di.AbstractGuiceInjector;

public class ServiceGuiceInjector extends ServiceInjector {
    public ServiceGuiceInjector() {
        this(new AbstractModule(){
           @Override
           protected void configure() {
               // bind nothing;
           }
        });
    }

    public ServiceGuiceInjector(AbstractModule binder) {
        super(new AbstractGuiceInjector(){
            @Override
            protected AbstractModule newBinder() {
                return binder;
            }
        });
    }
}
