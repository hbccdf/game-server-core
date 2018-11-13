package server.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import server.core.configuration.ConfigManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class ServiceProxy implements InvocationHandler {
    private final Class<?> serviceInterface;

    private final String configRootKey;

    private final RemoteServerConfig config;

    private TServiceClient client;

    private static final String INITIALIZE_NAME = "initialize";
    private static final String ISVALID_NAME = "isValid";
    private static final String RELEASE_NAME = "release";
    private static final String RELOAD_NAME = "reload";

    public ServiceProxy(Class<?> serviceInterface, String configRootKey) {
        this.serviceInterface = serviceInterface;
        this.configRootKey = configRootKey;
        this.config = ConfigManager.read(RemoteServerConfig.class, configRootKey);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            String methodName = method.getName();
            if (INITIALIZE_NAME.equals(methodName)) {
                try {
                    if (config == null) {
                        log.error("read server config {} failed", configRootKey);
                        return false;
                    }
                    buildConnection(config.getIp(), config.getPort());
                    return true;
                } catch (Exception e) {
                    log.error("initialize service {} error", serviceInterface, e);
                }
                return false;

            } else if (ISVALID_NAME.equals(methodName)) {
                return alive();
            } else if (RELEASE_NAME.equals(methodName)) {
                destroyConnection();
                return null;
            } else if(RELOAD_NAME.equals(methodName)) {
                return null;
            } else {
                if (!alive()) {
                    throw new IllegalStateException("remote service is not available: " + serviceInterface.getCanonicalName());
                }
                return method.invoke(client, args);
            }

            //todo reconnect server and destroy connection when TTransportException
        }
    }

    private boolean alive() {
        synchronized (this) {
            return client != null;
        }
    }

    private void destroyConnection() {
        synchronized (this) {
            log.info("{} Destroyed connection", serviceInterface.getName());
            client.getOutputProtocol().getTransport().close();
            client = null;
        }
    }

    private void buildConnection(String ip, int port) throws Exception {
        synchronized (this) {
            client = buildClient(serviceInterface, ip, port);
            log.info("Established connection {}:{}", ip, port);
        }
    }

    private TServiceClient buildClient(Class<?> si, String ip, int port) throws ClassNotFoundException, TTransportException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String serviceName = si.getName();
        Class<?> clazzClient = Class.forName(serviceName.substring(0, serviceName.lastIndexOf("$")) + "$Client");
        TTransport transport = new TFramedTransport(new TSocket(ip, port), 10240);
        transport.open();
        TProtocol p = new TCompactProtocol(transport);
        Constructor<?> constructor = clazzClient.getConstructor(TProtocol.class);
        return (TServiceClient) constructor.newInstance(new TMultiplexedProtocol(p, si.getCanonicalName()));
    }
}
