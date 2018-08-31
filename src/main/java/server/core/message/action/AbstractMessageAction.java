package server.core.message.action;

import io.netty.channel.ChannelHandlerContext;
import network.protocol.InternalMessage;
import org.apache.thrift.TBase;
import server.core.util.ThriftUtil;

public abstract class AbstractMessageAction<T extends TBase<?, ?>> extends AbstractGenericMessageAction {

    public AbstractMessageAction(int id) {
        super(id);
    }

    @Override
    public void processMessage(ChannelHandlerContext ctx, Object req) throws Exception {
        InternalMessage msg = (InternalMessage)req;
        T tpl = newObject();
        ThriftUtil.unmarshal(tpl, msg.getData());
        process(ctx, msg.getConnId(), tpl);
    }

    public void process(ChannelHandlerContext ctx, int connId, T req) throws Exception {
        process(connId, req);
    }

    public abstract void process(int connId, T req) throws Exception;

    public abstract T newObject();
}
