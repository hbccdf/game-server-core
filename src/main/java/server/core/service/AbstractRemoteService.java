package server.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.core.configuration.ConfigManager;
import server.core.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class AbstractRemoteService extends AbstractService {
    private static final String THRIFT_IFACE = "Iface";
    private static final String THRIFT_PROCESSOR = "Processor";

    private static final HashMap<Integer, TMultiplexedProcessor> processors = new HashMap<>();

    private final String configRootKey;

    private final String className = this.getClass().getName();

    private Thread thread = null;

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
            initService(multiProcessor);
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

    private void initService(TMultiplexedProcessor multiProcessor) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Class<?>> interfaces = ClassUtil.getInterface(this.getClass(), true);
        for (Class<?> clzz : interfaces) {
            String clzzName = clzz.getName();
            if (clzzName.indexOf(THRIFT_IFACE) > 0) {
                String processorTypeName = clzzName.replace(THRIFT_IFACE, THRIFT_PROCESSOR);
                Class<?> processorClass = Class.forName(processorTypeName);
                Constructor<?> constructor = processorClass.getConstructor(clzz);
                TProcessor processor = (TProcessor) constructor.newInstance(this);
                multiProcessor.registerProcessor(clzz.getCanonicalName(), processor);
                break;
            }
        }
    }
}
