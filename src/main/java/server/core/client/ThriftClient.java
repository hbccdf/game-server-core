package server.core.client;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.configuration.ConfigManager;
import server.core.service.RemoteServerConfig;

import java.lang.reflect.Constructor;

public class ThriftClient {
    private static final Logger logger = LoggerFactory.getLogger(ThriftClient.class);

    public static <T> T stub(String ip, int port, Class<T> clz) {
        try {
            int idx = clz.getName().lastIndexOf('$');
            String clzClientName = clz.getName().substring(0, idx) + "$Client";
            Class clzClient = Class.forName(clzClientName);

            TTransport transport = new TFramedTransport(new TSocket(ip, port), 10240);
            TProtocol p = new TCompactProtocol(transport);
            Constructor<T> constructor = clzClient.getConstructor(TProtocol.class);
            TServiceClient baseClient = (TServiceClient) constructor.newInstance(new TMultiplexedProtocol(p, clz.getCanonicalName()));
            return ReconnectingThriftClient.wrap(baseClient);
        } catch (Exception e) {
            logger.error("proxy client error: {}", clz, e);
        }
        return null;
    }

    public static <T> T stub(Class<T> clz, String configRootKey) {
        RemoteServerConfig config = ConfigManager.readProfile(RemoteServerConfig.class, configRootKey);
        if (config == null) {
            return null;
        }

        return stub(config.getIp(), config.getPort(), clz);
    }
}
