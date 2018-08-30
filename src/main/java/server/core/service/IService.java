package server.core.service;

public interface IService {
    boolean initialize();
    boolean isValid();
    void release();
}
