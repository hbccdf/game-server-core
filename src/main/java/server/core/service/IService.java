package server.core.service;

import server.core.configuration.IReloadable;

public interface IService extends IReloadable {
    boolean initialize();
    boolean isValid();
    void release();
}
