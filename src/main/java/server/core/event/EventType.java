package server.core.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class EventType<T extends IEvent> {
    private List<IEventListener<T>> listeners = null;

    public EventType() {
        this(1);
    }

    public EventType(int capacity) {
        this.listeners = new ArrayList<IEventListener<T>>(capacity);
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
        Iterator<IEventListener<T>> it = listeners.iterator();
        while (it.hasNext()) {
            IEventListener<T> lst = it.next();
            if (lst != null) {
                lst.onEvent(event);
            }
        }
    }
}
