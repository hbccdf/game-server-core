package server.core.event;

public interface IEventAggregator {
    <T extends IEvent> EventType<T> getEvent(Class<T> eventClass);
    <T extends IEvent> EventType<T> regEvent(Class<T> eventClass);
    <T extends IEvent> EventType<T> remEvent(Class<T> eventClass);
}
