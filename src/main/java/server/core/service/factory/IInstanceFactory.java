package server.core.service.factory;

public interface IInstanceFactory {
    <T> T getInstance(Class<T> classType);

    void injectInstance(Object object);

    void regInstance(Class<?> clz, Object object);
}
