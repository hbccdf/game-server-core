package server.core.service;

import java.lang.reflect.Proxy;

public class ServiceClient {
    @SuppressWarnings("unchecked")
    public static final <T> T proxy(Class<T> serviceInterface, String configRootKey) {
        Object proxyObject = Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface, IService.class},
                new ServiceProxy(serviceInterface, configRootKey));
        return (T) proxyObject;
    }
}
