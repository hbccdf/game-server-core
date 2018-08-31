package server.core.event;

import io.netty.util.concurrent.EventExecutor;

public class SingleThreadEventAggregator extends AbstractEventAggregator {
    private EventExecutor executor;

    public SingleThreadEventAggregator(EventExecutor executor) {
        super();
        if(executor == null){
            throw new IllegalArgumentException("executor can't  be null. ");
        }
        this.executor = executor;
    }

    @Override
    protected <T extends IEvent> EventType<T> newEventType() {
        return new SingleThreadEventType<>(this);
    }

    public EventExecutor getExecutor(){
        return executor;
    }
}
