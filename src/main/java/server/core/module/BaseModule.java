package server.core.module;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.di.Binder;
import server.core.di.GuiceInjector;
import server.core.service.AbstractService;
import server.core.service.factory.AbstractServiceFactory;
import server.core.service.factory.ServiceInjector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaseModule implements IModule {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private LinkedHashMap<Class<?>, HashMap<Integer, Object>> services = new LinkedHashMap<>();

    private AbstractServiceFactory factory;

    public BaseModule(AbstractServiceFactory factory) {
        this.factory = factory;
    }

    public BaseModule(Binder binder) {
        this.factory = new ServiceInjector(new GuiceInjector() {

            @Override
            protected AbstractModule newBinder() {
                return binder;
            }
        });
    }

    @Override
    public void initialize() {
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
                            logger.info("Initialize service success: " + end.getKey() + ":" + service.getKey().getCanonicalName());
                        } else {
                            logger.info("Fail initialize service: " + end.getKey() + ":" + service.getKey().getCanonicalName());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail initialize service: " + end.getKey() + ":" + service.getKey().getCanonicalName(), e);
                }
            }
        }
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
                        logger.info("Release service success: " + s.getClass().getCanonicalName());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Fail release service:"+s.getClass().getCanonicalName(), e);
                }
            }
        }
        services.clear();
    }

    protected <T> T newService(Class<T> serviceType, int endpoint) {
        T s = factory.newService(serviceType, endpoint);
        if (s instanceof AbstractService) {
            ((AbstractService) s).setHolder(this);
        }
        return s;
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
        HashMap<Integer, Object> serviceMap = services.get(serviceType);
        if (serviceMap == null) {
            serviceMap = new HashMap<>();
            services.put(serviceType, serviceMap);
        }
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
}
