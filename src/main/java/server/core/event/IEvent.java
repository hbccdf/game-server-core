package server.core.event;

public interface IEvent<T extends IEventArgs> {
    int getType();
}
