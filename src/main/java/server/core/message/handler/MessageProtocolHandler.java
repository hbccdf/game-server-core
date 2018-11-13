package server.core.message.handler;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import network.handler.IProtocolHandler;
import network.protocol.BaseMessage;
import server.core.message.action.IMessageAction;
import server.core.message.registry.IMessageActionRegistry;

@Slf4j
public class MessageProtocolHandler<T extends BaseMessage<?>> implements IProtocolHandler<T> {

    @Inject
    private IMessageActionRegistry<Integer> registry;

    public MessageProtocolHandler() {
        super();
    }

    @Override
    public void sessionOpened(ChannelHandlerContext ctx) {
        log.info("session opened: {}", ctx);
    }

    @Override
    public void sessionClosed(ChannelHandlerContext ctx) {
        log.info("session closed: {}", ctx);
        ctx.close();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, T msg) {
        log.debug("received msg {}, {}", ctx, msg);
        IMessageAction handler = registry.get(msg.getCmdId());
        if (handler != null) {
            try {
                handler.processMessage(ctx, msg);
            } catch (Exception e) {
                log.error("fail processMessage message {}", msg, e);
            }
        } else {
            log.error("can't find message handler, command id={}", msg.getCmdId());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exception caught: ", cause);
        ctx.close();
    }

    @Override
    public void release() {
        if (registry != null) {
            registry.release();
        }
    }
}
