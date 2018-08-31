package server.core.message.handler;

import io.netty.channel.ChannelHandlerContext;
import network.handler.IProtocolHandler;
import network.protocol.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.message.action.IMessageAction;
import server.core.message.registry.IMessageActionRegistry;

public class MessageProtocolHandler<T extends BaseMessage<?>> implements IProtocolHandler<T> {
    private static Logger logger = LoggerFactory.getLogger(MessageProtocolHandler.class);
    private IMessageActionRegistry<Integer> registry = null;

    public MessageProtocolHandler(IMessageActionRegistry<Integer> registry) {
        super();
        this.registry = registry;
    }

    @Override
    public void sessionOpened(ChannelHandlerContext ctx) {
        logger.info("session opened: {}", ctx);
    }

    @Override
    public void sessionClosed(ChannelHandlerContext ctx) {
        logger.info("session closed: {}", ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, T msg) {
        logger.debug("received msg {}, {}", ctx, msg);
        if (registry != null) {
            IMessageAction handler = registry.get(msg.getCmdId());
            if (handler != null) {
                try {
                    handler.processMessage(ctx, msg);
                } catch (Exception e) {
                    logger.error("fail processMessage message {}", msg, e);
                }
            } else {
                logger.error("can't find message handler, command id={}", msg.getCmdId());
            }
        } else {
            logger.error("message handler registry is null");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exception caught: ", cause);
    }

    @Override
    public void release() {
        if (registry != null) {
            registry.release();
        }
    }
}
