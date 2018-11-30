package server.core.service;

import java.lang.reflect.Proxy;

public class ServiceClient {
    public static <T> T proxy(Class<T> serviceInterface) {
        return proxy(serviceInterface, true);
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxy(Class<T> serviceInterface, boolean waitConnected) {
        Object proxyObject = Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface, IService.class},
                new ServiceProxy(serviceInterface, waitConnected));
        return (T) proxyObject;
    }
}
