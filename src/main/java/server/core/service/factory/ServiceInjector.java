package server.core.service.factory;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import server.core.di.IInjector;
import server.core.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class ServiceInjector extends AbstractServiceFactory {
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
    public <T> T getInstance(Class<T> classType) {
        return injector.getInstance(classType);
    }

    @Override
    public void injectInstance(Object object) {
        if (object == null) {
            return;
        }

        try {
            List<Field> fileds = ClassUtil.getFieldsWithAllAnnotations(object.getClass(), true, Inject.class);
            for (Field f : fileds) {
                f.setAccessible(true);
                Object injectObject = injector.getInstance(f.getType());
                f.set(object, injectObject);
            }
        } catch (Exception e) {
            log.error("inject object {} failed", object.getClass().getName(), e);
        }
    }

    @Override
    public void regInstance(Class<?> clz, Object object) {
        injector.regInstance(clz, object);
    }
}
