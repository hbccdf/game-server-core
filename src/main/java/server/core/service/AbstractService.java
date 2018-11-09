package server.core.service;

import com.google.inject.Inject;
import server.core.module.IServiceHolder;
import server.core.service.factory.IInstanceFactory;

public abstract class AbstractService implements IService {

    @Inject
    protected IServiceHolder holder;

    @Inject
    protected IInstanceFactory factory;

    public IServiceHolder getHolder() {
        return holder;
    }

    public IInstanceFactory getFactory() {
        return factory;
    }

    public <T> T getService(Class<T> clz) {
        return holder.getService(clz);
    }

    public void update(long now) {

    }
}
