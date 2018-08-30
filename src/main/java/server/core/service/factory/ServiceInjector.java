package server.core.service.factory;

import server.core.di.IInjector;

public class ServiceInjector extends AbstractServiceFactory {
    private IInjector injector;

    public ServiceInjector(IInjector injector) {
        super();
        this.injector = injector;
    }

    @Override
    public <T> T newService(Class<T> serviceType, int endpoint) {
        return injector.getInstance(serviceType);
    }
}
