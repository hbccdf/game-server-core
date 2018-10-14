package server.core.service.factory;

public interface IInstaceFactory {
    <T> T newInstace(Class<T> classType);

    void injectInstance(Object object);

    void regInstance(Class<?> clz, Object object);
}
