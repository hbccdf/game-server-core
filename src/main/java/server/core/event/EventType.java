package server.core.event;

import java.util.ArrayList;
import java.util.List;

public abstract class EventType<T extends IEvent> {
    private List<IEventListener<T>> listeners;

    public EventType() {
        this(1);
    }

    public EventType(int capacity) {
        this.listeners = new ArrayList<>(capacity);
    }

    public void subscribe(IEventListener<T> listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    public void publish(T event) {
        publish0(event);
    }

    protected final void publish0(T event) {
        for (IEventListener<T> lst : listeners) {
            if (lst != null) {
                lst.onEvent(event);
            }
        }
    }
}
