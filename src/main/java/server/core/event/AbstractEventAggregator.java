package server.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class AbstractEventAggregator implements IEventAggregator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEventAggregator.class);

    private HashMap<Class<?>, EventType<IEvent>> registry = new HashMap<>();

    private EventType<IEvent> nope = new EventType<IEvent>() {
        @Override
        public void subscribe(IEventListener<IEvent> listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void publish(IEvent event) {
            logger.warn("event was not bean listened, event= {}", event);
        }
    };

    @Override
    public <T extends IEvent> EventType<T> getEvent(Class<T> eventClass) {
        EventType<IEvent> list;
        for (Class<? super T> clz = eventClass; clz != Object.class; clz = clz.getSuperclass()) {
            list = registry.get(clz);
            if (list != null) {
                return ((EventType<T>) list);
            }
        }
        return ((EventType<T>) nope);
    }

    @Override
    public <T extends IEvent> EventType<T> regEvent(Class<T> eventClass) {
        EventType<IEvent> eventType = registry.get(eventClass);
        if (eventType == null) {
            eventType = newEventType();
            registry.put(eventClass, eventType);
        }
        return (EventType<T>) eventType;
    }

    @Override
    public <T extends IEvent> EventType<T> remEvent(Class<T> eventClass) {
        return (EventType<T>)registry.remove(eventClass);
    }

    protected abstract <T extends IEvent> EventType<T> newEventType();
}
