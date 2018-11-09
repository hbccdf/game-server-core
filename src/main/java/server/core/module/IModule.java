package server.core.module;

import server.core.service.factory.IInstanceFactory;

public interface IModule extends IServiceHolder {
    boolean initialize();
    boolean reload();
    void release();
    void update(long now);

    IInstanceFactory getInstanceFactory();
}
