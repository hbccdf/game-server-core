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
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import server.core.module.ModuleManager;
import server.core.service.zk.EndPoint;
import server.core.service.zk.IZkService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class ServiceProxy implements InvocationHandler {
    private final Class<?> serviceInterface;

    private TServiceClient client;
    private EndPoint endPoint;

    private IZkService zkService;

    private static final String INITIALIZE_NAME = "initialize";
    private static final String ISVALID_NAME = "isValid";
    private static final String RELEASE_NAME = "release";
    private static final String RELOAD_NAME = "reload";

    public ServiceProxy(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (this) {
            String methodName = method.getName();
            if (INITIALIZE_NAME.equals(methodName)) {
                return init();

            } else if (ISVALID_NAME.equals(methodName)) {
                return isValid();
            } else if (RELEASE_NAME.equals(methodName)) {
                release();
                return null;
            } else if(RELOAD_NAME.equals(methodName)) {
                return reload();
            } else {
                if (!alive()) {
                    throw new IllegalStateException("remote service is not available: " + serviceInterface.getCanonicalName());
                }
                return method.invoke(client, args);
            }
        }
    }

    private boolean init() {
        try {
            zkService = ModuleManager.INSTANCE.getFactory().getInstance(IZkService.class);
            Stat stat = zkService.get().exists("/Service/" + serviceInterface.getCanonicalName(), event -> {
                if (event.getType() == Watcher.Event.EventType.NodeCreated) {
                    find(serviceInterface);
                }
            });

            if (stat != null) {
                find(serviceInterface);
            }
            return true;
        } catch (Exception e) {
            log.error("initialize service {} error", serviceInterface, e);
        }
        return false;
    }

    private boolean isValid() {
        return alive();
    }

    private void release() {
        if (endPoint != null) {
            destroyConnection(endPoint.getId());
        }
    }

    private boolean reload() {
        return true;
    }

    private void find(Class<?> clzz) {
        try {
            List<String> children = zkService.get().getChildren("/Service/" + clzz.getCanonicalName(), event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    if (!alive()) {
                        find(clzz);
                    }
                }
            });

            if (children.size() > 0) {
                String endpoint = children.get(0);
                buildConnection(Integer.parseInt(endpoint));
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private boolean alive() {
        synchronized (this) {
            return client != null;
        }
    }

    private void destroyConnection(int endpointId) {
        synchronized (this) {
            try {
                if (endPoint != null && endPoint.getId() == endpointId) {
                    log.info("{} Destroyed connection, {}", serviceInterface.getName(), endpointId);
                    client.getOutputProtocol().getTransport().close();
                    client = null;
                    endPoint = null;
                }
            } catch (Exception ignored) {

            }
        }
    }

    private void buildConnection(int endpointId) throws Exception {
        synchronized (this) {
            if (endPoint != null) {
                return;
            }

            byte[] data = zkService.get().getData("/Service/" + serviceInterface.getCanonicalName() + "/" + endpointId, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    destroyConnection(endpointId);
                } else if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    destroyConnection(endpointId);
                    find(serviceInterface);
                }
            }, null);

            EndPoint ep = EndPoint.decode(data);

            client = buildClient(serviceInterface, ep.getIp(), ep.getPort());
            this.endPoint = ep;
            log.info("Established connection {}", endPoint);
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
