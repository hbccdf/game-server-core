package server.core.module;

public interface IModule extends IServiceHolder {
    void initialize();
    void release();
    void update(long now);
}
