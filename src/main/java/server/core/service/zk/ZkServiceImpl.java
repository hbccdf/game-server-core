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
    private ZooKeeper zk;

    @Override
    public boolean initialize() {
        return initZk();
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

    private boolean initZk() {
        try {
            ZkConfig config = ConfigManager.read(ZkConfig.class, "zookeeper");
            zk = new ZooKeeper(getConnectString(config), config.getSessionTimeout(), event -> {
                if (event.getState() == Watcher.Event.KeeperState.Expired) {
                    log.error("zk session expired");
                    try {
                        zk.close();
                        zk = null;
                    } catch (InterruptedException ignored) {
                    }

                    log.info("try to reconnect zk center");
                    initZk();
                }
            });
            return true;
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    private String getConnectString(ZkConfig config) {
        if (!config.getConnectString().contains(":")) {
            return ConfigManager.getString("systemIp", "127.0.0.1") + ":" + config.getConnectString();
        } else {
            return config.getConnectString();
        }
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
