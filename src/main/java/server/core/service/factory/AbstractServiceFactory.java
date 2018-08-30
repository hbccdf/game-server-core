package server.core.service.factory;

public abstract class AbstractServiceFactory {
    public abstract <T> T newService(Class<T> serviceType, int endpoint);
}
