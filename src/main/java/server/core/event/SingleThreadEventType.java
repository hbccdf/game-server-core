package server.core.event;

public class SingleThreadEventType<T extends IEvent> extends EventType<T> {
    private SingleThreadEventAggregator eventSource;

    public SingleThreadEventType(SingleThreadEventAggregator eventSource) {
        super();
        this.eventSource = eventSource;
    }

    @Override
    public void publish(final T event) {
        eventSource.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                publish0(event);
            }
        });
    }
}
