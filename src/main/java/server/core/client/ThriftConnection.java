package server.core.client;

import network.client.TcpConnection;
import network.handler.IProtocolHandler;
import network.protocol.DefaultMessage;
import network.protocol.codec.DefaultMessageCodecFactory;
import org.apache.thrift.TBase;
import server.core.util.ThriftUtil;

public class ThriftConnection extends TcpConnection<DefaultMessage> {
    public ThriftConnection(String ip, int port, IProtocolHandler<DefaultMessage> handler) {
        super(ip, port, DefaultMessageCodecFactory.INSTANCE, handler);
    }

    public <T extends TBase<?, ?>> void write(int protoId, T obj) {
        try {
            DefaultMessage msg = new DefaultMessage(protoId, ThriftUtil.marshal(obj));
            super.write(msg);
        } catch (Exception e) {

        }
    }
}