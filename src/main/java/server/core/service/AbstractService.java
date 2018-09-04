package server.core.service;

import com.google.inject.Inject;
import server.core.module.IServiceHolder;
import server.core.service.factory.IInstaceFactory;

public abstract class AbstractService implements IService {

    @Inject
    private IServiceHolder holder;

    @Inject
    private IInstaceFactory factory;

    public IServiceHolder getHolder() {
        return holder;
    }

    public IInstaceFactory getFactory() {
        return factory;
    }

    public <T> T getService(Class<T> clz) {
        return holder.getService(clz);
    }

    public void update(long now) {

    }
}
