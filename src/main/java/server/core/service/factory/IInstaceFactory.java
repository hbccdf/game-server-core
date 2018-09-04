package server.core.service.factory;

public interface IInstaceFactory {
    <T> T newInstace(Class<T> classType);
}
