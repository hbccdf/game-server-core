package server.core.service.zk;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import server.core.configuration.ConfigManager;
import server.core.service.AbstractService;

@Slf4j
@Singleton
public class ZkServiceImpl extends AbstractService implements IZkService {
    private static final String PRIFIX = "/Service/";

    private ZooKeeper zk;

    @Override
    public boolean initialize() {
        try {
            ZkConfig config = ConfigManager.read(ZkConfig.class, "zookeeper");
            zk = new ZooKeeper(config.getConnectString(), config.getSessionTimeout(), event -> {
                if (event.getType() != Watcher.Event.EventType.None) {
                    log.info("{}:{}", event.getType(), event.getPath());
                }
            });
            return true;
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void release() {
        if (zk != null) {
            try {
                zk.close();
            } catch (Exception e) {
                log.error("", e);
            }
            zk = null;
        }
    }

    @Override
    public boolean reload() {
        return true;
    }

    @Override
    public ZooKeeper get() {
        return zk;
    }

    public static class ZkConfig {
        private String connectString;
        private int sessionTimeout;

        public String getConnectString() {
            return connectString;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }
    }

}
