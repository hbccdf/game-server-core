package server.core.message.registry;

import server.core.message.action.IMessageAction;
import server.core.module.IServiceHolder;

import java.util.HashMap;

public class AbstractMessageActionRegistry<T> implements IMessageActionRegistry<T> {
    protected IServiceHolder holder;
    private HashMap<T, IMessageAction> handlers = new HashMap<>();

    public AbstractMessageActionRegistry(IServiceHolder holder) {
        super();
        this.holder = holder;
    }

    @Override
    public IMessageAction get(T command) {
        return handlers.get(command);
    }

    @Override
    public void add(T command, IMessageAction handler) {
        handlers.put(command, handler);
    }

    @Override
    public void remove(T command) {
        handlers.remove(command);
    }

    @Override
    public boolean contain(T command) {
        return handlers.containsKey(command);
    }

    @Override
    public void release() {
        handlers.clear();
    }
}
