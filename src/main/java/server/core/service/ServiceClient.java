package server.core.service;

import java.lang.reflect.Proxy;

public class ServiceClient {
    @SuppressWarnings("unchecked")
    public static <T> T proxy(Class<T> serviceInterface) {
        Object proxyObject = Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface, IService.class},
                new ServiceProxy(serviceInterface));
        return (T) proxyObject;
    }
}
