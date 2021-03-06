package server.core.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import server.core.configuration.ConfigManager;
import server.core.service.RemoteServerConfig;

import java.lang.reflect.Constructor;

@Slf4j
public class ThriftClient {
    public static <T> T stub(Class<T> clz, String ip, int port, int numRetries, int timeBetweenRetry) {
        return internalStub(ip, port, clz, new ReconnectingThriftClient.Options(numRetries, timeBetweenRetry));
    }

    public static <T> T stub(Class<T> clz, String ip, int port) {
        return internalStub(ip, port, clz, ReconnectingThriftClient.Options.defaults());
    }

    public static <T> T stub(Class<T> clz, String configRootKey) {
        RemoteServerConfig config = ConfigManager.read(RemoteServerConfig.class, configRootKey);
        if (config == null) {
            return null;
        }

        return stub(clz, config.getIp(), config.getPort());
    }

    public static <T> T stub(Class<T> clz) {
        return stub(clz, "backServer");
    }


    private static <T> T internalStub(String ip, int port, Class<T> clz, ReconnectingThriftClient.Options options) {
        try {
            int idx = clz.getName().lastIndexOf('$');
            String clzClientName = clz.getName().substring(0, idx) + "$Client";
            Class<?> clzClient = Class.forName(clzClientName);

            TTransport transport = new TFramedTransport(new TSocket(ip, port));
            TProtocol p = new TCompactProtocol(transport);
            Constructor<?> constructor = clzClient.getConstructor(TProtocol.class);
            TServiceClient baseClient = (TServiceClient) constructor.newInstance(new TMultiplexedProtocol(p, clz.getCanonicalName()));
            return ReconnectingThriftClient.wrap(baseClient, options);
        } catch (Exception e) {
            log.error("proxy client error: {}", clz, e);
        }
        return null;
    }
}
