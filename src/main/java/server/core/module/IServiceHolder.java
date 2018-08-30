package server.core.module;

public interface IServiceHolder {
    <T> T getService(Class<T> serviceType);

    <T> T getService(Class<T> serviceType, int endpoint);

    <T> T regService(Class<T> serviceType);

    <T> T regService(Class<T> serviceType, int endpoint);

    <T> T remService(Class<T> serviceType);

    <T> T remService(Class<T> serviceType, int endpoint);
}
