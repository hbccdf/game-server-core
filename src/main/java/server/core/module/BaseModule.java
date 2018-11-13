package server.core.module;

import com.google.inject.AbstractModule;
import lombok.extern.slf4j.Slf4j;
import server.core.configuration.IReloadable;
import server.core.di.BaseBinder;
import server.core.di.AbstractGuiceInjector;
import server.core.di.IInjector;
import server.core.service.AbstractService;
import server.core.service.factory.AbstractServiceFactory;
import server.core.service.factory.IInstanceFactory;
import server.core.service.factory.ServiceInjector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
public class BaseModule implements IModule {
    private final LinkedHashMap<Class<?>, HashMap<Integer, Object>> services = new LinkedHashMap<>();

    private AbstractServiceFactory factory;

    public BaseModule(AbstractServiceFactory factory) {
        this.factory = factory;
    }

    public BaseModule(BaseBinder baseBinder) {

        IServiceHolder holder = this;
        ServiceInjector serviceInjector = new ServiceInjector();
        setFactory(serviceInjector);

        IInjector injector = new AbstractGuiceInjector() {

            @Override
            protected AbstractModule newBinder() {
                return baseBinder;
            }

            @Override
            protected AbstractModule[] newBinders() {
                AbstractModule[] modules = new AbstractModule[2];
                modules[0] = newBinder();

                modules[1] = new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IInstanceFactory.class).toInstance(factory);
                        bind(IServiceHolder.class).toInstance(holder);
                    }
                };
                return modules;
            }
        };
        serviceInjector.setInjector(injector);
    }

    @Override
    public boolean initialize() {
        for (Map.Entry<Class<?>, HashMap<Integer, Object>> service : services.entrySet()) {
            for (Map.Entry<Integer, Object> end : service.getValue().entrySet()) {
                Method method = null;
                try {
                    method = end.getValue().getClass().getMethod("initialize");
                } catch (Exception e) {
                    // do nothing
                }
                try {
                    if (method != null) {
                        if ((boolean) method.invoke(end.getValue())) {
                            log.info("Initialize service success: {}:{}", end.getKey(), service.getKey().getCanonicalName());
                        } else {
                            log.error("Fail initialize service: {}:{}", end.getKey(), service.getKey().getCanonicalName());
                            return false;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail initialize service: " + end.getKey() + ":" + service.getKey().getCanonicalName(), e);
                }
            }
        }
        return true;
    }

    @Override
    public boolean reload() {
        for (HashMap<Integer, Object> endpoint : services.values()) {
            for (Object s : endpoint.values()) {
                if (s instanceof IReloadable) {
                    IReloadable r = (IReloadable) s;
                    try {
                        if (!r.reload()) {
                            log.warn("service reload failed: {}", s.getClass().getCanonicalName());
                        }
                    } catch (Exception e) {
                        log.error("service reload error: {}", s.getClass().getCanonicalName());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void release() {
        for (HashMap<Integer, Object> endpoint : services.values()) {
            for (Object s : endpoint.values()) {
                Method method = null;
                try {
                    method = s.getClass().getMethod("release");
                } catch (Exception e) {
                    // do nothing
                }
                try {
                    if (method != null) {
                        method.invoke(s);
                        log.info("Release service success: " + s.getClass().getCanonicalName());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail release service:" + s.getClass().getCanonicalName(), e);
                }
            }
        }
        services.clear();
    }

    protected <T> T newService(Class<T> serviceType, int endpoint) {
        return factory.newService(serviceType, endpoint);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> serviceType, int endpoint) {
        HashMap<Integer, Object> serviceMap = services.get(serviceType);
        return serviceMap == null ? null : (T)serviceMap.get(endpoint);
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        return getService(serviceType, 0);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T regService(Class<T> serviceType, int endpoint) {
        HashMap<Integer, Object> serviceMap = services.computeIfAbsent(serviceType, k -> new HashMap<>(16));
        return (T) serviceMap.put(0, newService(serviceType, 0));
    }

    @Override
    public <T> T regService(Class<T> serviceType) {
        return regService(serviceType, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T remService(Class<T> serviceType, int endpoint) {
        HashMap<Integer, Object> serviceMap = services.get(serviceType);
        return serviceMap == null ? null : (T) serviceMap.remove(0);
    }

    @Override
    public <T> T remService(Class<T> serviceType) {
        return regService(serviceType, 0);
    }

    @Override
    public void update(long now) {
        for (HashMap<Integer, Object> endpoint : services.values()) {
            for (Object s : endpoint.values()) {
                if (s instanceof AbstractService) {
                    ((AbstractService) s).update(now);
                }
            }
        }
    }

    @Override
    public IInstanceFactory getInstanceFactory() {
        return this.factory;
    }

    protected void setFactory(AbstractServiceFactory factory) {
        this.factory = factory;
    }
}
