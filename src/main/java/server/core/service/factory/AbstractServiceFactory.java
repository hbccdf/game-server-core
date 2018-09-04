package server.core.service.factory;

public abstract class AbstractServiceFactory implements IInstaceFactory {
    public abstract <T> T newService(Class<T> serviceType, int endpoint);
}
