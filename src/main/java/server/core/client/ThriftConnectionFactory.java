package server.core.client;

import network.client.TcpConnectionFactory;
import network.handler.IProtocolHandler;
import network.protocol.DefaultMessage;
import network.protocol.codec.DefaultMessageCodecFactory;

public class ThriftConnectionFactory extends TcpConnectionFactory<DefaultMessage> {
    public ThriftConnectionFactory(String ip, int port, IProtocolHandler<DefaultMessage> handler) {
        super(ip, port, DefaultMessageCodecFactory.INSTANCE, handler);
    }
}
