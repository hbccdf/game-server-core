package server.core.event;

public interface IEventListener<T extends IEvent> {
    void onEvent(T event);
}
