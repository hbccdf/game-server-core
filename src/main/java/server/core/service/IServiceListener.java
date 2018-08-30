package server.core.service;

public interface IServiceListener {
    void onAdd(String path);

    void onRemove(String path);

    void onUpdate(String path);
}
