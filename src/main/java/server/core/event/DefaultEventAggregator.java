package server.core.event;

public class DefaultEventAggregator extends AbstractEventAggregator {
    @Override
    public <T extends IEvent> EventType<T> newEventType() {
        return new DefaultEventType<>();
    }
}
