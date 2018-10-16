package server.core.service;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxy.class);

    private Class<?> serviceInterface;

    private TServiceClient client = null;

    public ServiceProxy(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            String methodName = method.getName();
            if ("initialize".equals(methodName)) {
                try {
                    buildConnection("127.0.0.1", 8855);
                    return true;
                } catch (Exception e) {
                    logger.error("initialize service {} error", serviceInterface, e);
                }
                return false;

            } else if ("isValid".equals(methodName)) {
                return alive();
            } else if ("release".equals(methodName)) {
                destroyConnection();
                return null;
            } else {
                if (!alive()) {
                    throw new IllegalStateException("remote service is not avaliable: " + serviceInterface.getCanonicalName());
                }
                return method.invoke(client, args);
            }
        }
    }

    private boolean alive() {
        synchronized (this) {
            return client != null;
        }
    }

    private void destroyConnection() {
        synchronized (this) {
            logger.info("{} Destroyed connection", serviceInterface.getName());
            client.getOutputProtocol().getTransport().close();
            client = null;
        }
    }

    private void buildConnection(String ip, int port) throws Exception {
        synchronized (this) {
            client = buildClient(serviceInterface, ip, port);
            logger.info("Established connection {}:{}", ip, port);
        }
    }

    private TServiceClient buildClient(Class<?> si, String ip, int port) throws ClassNotFoundException, TTransportException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String serviceName = si.getName();
        Class clazzClient = Class.forName(serviceName.substring(0, serviceName.lastIndexOf("$")) + "$Client");
        TTransport transport = new TFramedTransport(new TSocket(ip, port), 10240);
        transport.open();
        TProtocol p = new TCompactProtocol(transport);
        Constructor constructor = clazzClient.getConstructor(TProtocol.class);
        return (TServiceClient) constructor.newInstance(new TMultiplexedProtocol(p, si.getCanonicalName()));
    }
}
