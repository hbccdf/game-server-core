package server.core.event;

public abstract class BaseEvent<T extends IEventArgs> implements IEvent<T> {
    private T args;
    private Object source;

    public BaseEvent(Object source, T args) {
        super();
        this.args = args;
        this.source = source;
    }

    public T getEventArgs() {
        return args;
    }

    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "event type= " +this.getType()+", event class= " + this.getClass();
    }
}
