package server.core.service.zk;

import com.google.inject.ImplementedBy;
import org.apache.zookeeper.ZooKeeper;

@ImplementedBy(ZkServiceImpl.class)
public interface IZkService {
    ZooKeeper get();
}
