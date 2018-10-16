package server.core.module;

public interface IModule extends IServiceHolder {
    boolean initialize();
    void release();
    void update(long now);
}
