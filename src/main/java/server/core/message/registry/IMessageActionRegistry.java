package server.core.message.registry;

import server.core.message.action.IMessageAction;

public interface IMessageActionRegistry<T> {
    IMessageAction get(T command);

    void add(T command, IMessageAction handler);

    void remove(T command);

    boolean contain(T command);

    void release();
}
