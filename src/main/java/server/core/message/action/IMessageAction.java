package server.core.message.action;

import io.netty.channel.ChannelHandlerContext;

public interface IMessageAction {
    Integer getId();

    void processMessage(ChannelHandlerContext ctx, Object object) throws Exception;
}
