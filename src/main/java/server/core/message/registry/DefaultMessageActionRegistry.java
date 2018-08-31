package server.core.message.registry;

import server.core.module.IServiceHolder;

public class DefaultMessageActionRegistry<T> extends AbstractMessageActionRegistry<T> {
    public DefaultMessageActionRegistry(IServiceHolder holder) {
        super(holder);
    }
}
