package server.core.message.action;

import io.netty.channel.ChannelHandlerContext;
import network.protocol.InternalMessage;
import org.apache.thrift.TBase;

public abstract class AbstractInternalMessageAction<T extends TBase<?, ?>> extends AbstractBaseMessageAction<T, InternalMessage> {

    public AbstractInternalMessageAction(int id) {
        super(id);
    }

    @Override
    protected int getConnId(ChannelHandlerContext ctx, InternalMessage msg) {
        return msg.getConnId();
    }
}
