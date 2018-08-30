package server.core.service;

import server.core.module.IServiceHolder;

public abstract class AbstractService implements IService {

    private IServiceHolder holder;

    public void setHolder(IServiceHolder holder) {
        this.holder = holder;
    }

    public IServiceHolder getHolder() {
        return holder;
    }

    public <T> T getService(Class<T> clz) {
        return holder.getService(clz);
    }

    public void update(long now) {

    }
}
