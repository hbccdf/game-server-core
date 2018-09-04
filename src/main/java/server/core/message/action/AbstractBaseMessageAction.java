package server.core.message.action;

import io.netty.channel.ChannelHandlerContext;
import network.protocol.BaseMessage;
import network.protocol.InternalMessage;
import org.apache.thrift.TBase;
import server.core.util.ThriftUtil;

public abstract class AbstractBaseMessageAction<T extends TBase<?, ?>, M extends BaseMessage<?>> extends AbstractGenericMessageAction {

    public AbstractBaseMessageAction(int id) {
        super(id);
    }

    @Override
    public void processMessage(ChannelHandlerContext ctx, Object req) throws Exception {
        M msg = (M)req;
        T tpl = newObject();
        ThriftUtil.unmarshal(tpl, msg.getData());
        process(ctx, getConnId(ctx, msg), tpl);
    }

    public void process(ChannelHandlerContext ctx, int connId, T req) throws Exception {
        process(connId, req);
    }

    public abstract void process(int connId, T req) throws Exception;

    public abstract T newObject();

    protected abstract int getConnId(ChannelHandlerContext ctx, M msg);
}
