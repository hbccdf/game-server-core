package server.core.service.factory;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.di.IInjector;

import java.lang.reflect.Field;

public class ServiceInjector extends AbstractServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInjector.class);
    private IInjector injector;

    public ServiceInjector(IInjector injector) {
        super();
        this.injector = injector;
    }

    public ServiceInjector() {
        super();
    }

    public void setInjector(IInjector injector) {
        this.injector = injector;
    }

    @Override
    public <T> T newService(Class<T> serviceType, int endpoint) {
        return injector.getInstance(serviceType);
    }

    @Override
    public <T> T newInstace(Class<T> classType) {
        return injector.getInstance(classType);
    }

    @Override
    public void injectInstance(Object object) {
        if (object == null) {
            return;
        }

        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);

                    Object injectObject = injector.getInstance(field.getType());
                    field.set(object, injectObject);
                }
            }
        } catch (Exception e) {
            logger.error("inject object {} failed", object.getClass().getName(), e);
        }
    }
}
