package server.core.message.action;

import io.netty.channel.ChannelHandlerContext;
import server.core.module.IServiceHolder;

public abstract class AbstractGenericMessageAction implements IMessageAction {

    public static final String FIELD_NAME_HOLDER = "holder";
    private IServiceHolder holder;
    private int id;

    public AbstractGenericMessageAction(int id) {
        super();
        this.id = id;
    }

    public <T> T getService(Class<T> clz) {
        return holder.getService(clz);
    }

    @Override
    public Integer getId() {
        return id;
    }
}
