package server.core.scheduler;

import java.util.concurrent.*;

public class DefaultScheduler implements IScheduler {
    private ScheduledExecutorService threadPool = null;

    public DefaultScheduler(int nThreads) {
        threadPool = Executors.newScheduledThreadPool(nThreads);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return threadPool.schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return threadPool.schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return threadPool.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return threadPool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void release() {
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
    }
}
