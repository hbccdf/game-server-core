package server.core.module;

import server.core.service.factory.IInstaceFactory;

public interface IModule extends IServiceHolder {
    boolean initialize();
    void release();
    void update(long now);

    IInstaceFactory getInstanceFactory();
}
