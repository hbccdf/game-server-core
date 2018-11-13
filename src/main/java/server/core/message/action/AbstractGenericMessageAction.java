package server.core.message.action;

import com.google.inject.Inject;
import server.core.module.IServiceHolder;

public abstract class AbstractGenericMessageAction implements IMessageAction {

    @Inject
    private IServiceHolder holder;
    protected int id;

    public AbstractGenericMessageAction() {
        this.id = 0;
    }

    public <T> T getService(Class<T> clz) {
        return holder.getService(clz);
    }

    @Override
    public Integer getId() {
        return id;
    }
}
