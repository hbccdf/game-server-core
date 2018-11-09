package server.core.service.factory;

public abstract class AbstractServiceFactory implements IInstanceFactory {
    public abstract <T> T newService(Class<T> serviceType, int endpoint);
}
