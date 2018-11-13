package server.core.client;

import network.client.TcpConnectionFactory;
import network.handler.IProtocolHandler;
import network.initializer.DefaultProtocolInitializer;
import network.protocol.DefaultMessage;

public class ThriftConnectionFactory extends TcpConnectionFactory {
    public ThriftConnectionFactory(String ip, int port, IProtocolHandler<DefaultMessage> handler) {
        super(ip, port, new DefaultProtocolInitializer(handler));
    }
}
