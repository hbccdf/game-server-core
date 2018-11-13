package server.core;

public abstract class AbstractServer {
    private final int interval;
    private boolean running = true;

    public AbstractServer() {
        this(50);
    }

    public AbstractServer(int interval) {
        this.interval = interval;
    }

    public void startup(){
        startup(true);
    }

    public void startup(boolean loop){
        startup0();
        if (loop) {
            startLoop();
        }
    }

    public void shutdown(){
        running = false;
    }

    public abstract void onTick();

    private void startLoop(){
        running = true;
        while(running){
            try {
                onTick();
                Thread.sleep(interval);
            } catch (InterruptedException e) {
            }
        }
    }

    public abstract void startup0();
}
