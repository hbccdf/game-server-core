package server.core.service;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.*;
import server.core.configuration.ConfigManager;
import server.core.service.zk.EndPoint;
import server.core.service.zk.IZkService;
import server.core.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class AbstractRemoteService extends AbstractService {
    private static final String THRIFT_IFACE = "Iface";
    private static final String THRIFT_PROCESSOR = "Processor";

    public static final String LOCAL_HOST = "127.0.0.1";

    private static final HashMap<Integer, TMultiplexedProcessor> processors = new HashMap<>();

    private final String configRootKey;

    private final String className = this.getClass().getName();

    private Thread thread = null;

    @Inject
    private IZkService zkService;

    public AbstractRemoteService() {
        this("backServer");
    }

    public AbstractRemoteService(String configRootKey) {
        this.configRootKey = configRootKey;
    }

    @Override
    public boolean initialize() {
        RemoteServerConfig config = ConfigManager.read(RemoteServerConfig.class, configRootKey);
        if (config == null) {
            log.error("{} failed to get {} config", className, configRootKey);
            return false;
        }

        try {
            TMultiplexedProcessor multiProcessor = initServer(config.getPort());
            initService(multiProcessor, config);
            return true;
        } catch (Exception e) {
            log.error("fail start remote service {}", className, e);
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void release() {
    }

    @Override
    public boolean reload() {
        return true;
    }

    private TMultiplexedProcessor initServer(int port) throws TTransportException {
        TMultiplexedProcessor multiProcessor = processors.get(port);
        if (multiProcessor != null) {
            log.info("start remote server {}, at port {}", this.getClass().getName(), port);
            return multiProcessor;
        }

        multiProcessor = new TMultiplexedProcessor();
        processors.put(port, multiProcessor);

        TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(port);
        TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverSocket);
        args.transportFactory(new TFramedTransport.Factory(10200));
        args.protocolFactory(new TCompactProtocol.Factory());
        args.processor(multiProcessor);
        args.maxReadBufferBytes = 10240;
        args.selectorThreads(2);
        args.workerThreads(4);
        TServer server = new TThreadedSelectorServer(args);
        thread = new Thread(server::serve);
        thread.start();

        log.info("start remote server {}, at port {}", this.getClass().getName(), port);

        return multiProcessor;
    }

    private void initService(TMultiplexedProcessor multiProcessor, RemoteServerConfig config) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Class<?>> interfaces = ClassUtil.getInterface(this.getClass(), true);
        for (Class<?> clzz : interfaces) {
            String clzzName = clzz.getName();
            if (clzzName.indexOf(THRIFT_IFACE) > 0) {
                String processorTypeName = clzzName.replace(THRIFT_IFACE, THRIFT_PROCESSOR);
                Class<?> processorClass = Class.forName(processorTypeName);
                Constructor<?> constructor = processorClass.getConstructor(clzz);
                TProcessor processor = (TProcessor) constructor.newInstance(this);
                multiProcessor.registerProcessor(clzz.getCanonicalName(), processor);

                registerService(clzz, config);
                break;
            }
        }
    }

    private void registerService(Class<?> clzz, RemoteServerConfig config) {
        ZooKeeper zk = zkService.get();
        try {
            zk.create("/Service", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception ignored) {

        }

        try {
            zk.create("/Service/" + clzz.getCanonicalName(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception ignored) {

        }

        try {
            EndPoint ep = new EndPoint();
            ep.setId(config.getEndpoint());
            ep.setIp(getIp(config));
            ep.setPort(config.getPort());
            ep.setTimestamp(System.currentTimeMillis());

            String endpointName = "/Service/" + clzz.getCanonicalName() + "/" + config.getEndpoint();

            zk.create(endpointName, ep.encode(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            zk.exists(endpointName, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted || event.getState() == Watcher.Event.KeeperState.Expired) {
                    log.error("{}:{} disconnect with center, event {}", clzz.getCanonicalName(), config.getEndpoint(), event);
                    registerService(clzz, config);
                }
            });

            if (zk.exists(endpointName, false) != null) {
                log.info("success register to center {}:{}", clzz.getCanonicalName(), ep.getId());
            }
        } catch (Exception e) {
            log.error("duplicate service node, service={}, endpointId={}", clzz.getCanonicalName(), config.getEndpoint(), e);
        }
    }

    private String getIp(RemoteServerConfig config) {
        if (config.getIp() == null || LOCAL_HOST.equals(config.getIp())) {
            return ConfigManager.getString("systemIp", LOCAL_HOST);
        }

        return config.getIp();
    }
}
