package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.api.CommitLogStore;
import cn.coderule.minimq.store.server.ha.core.HAContext;
import cn.coderule.minimq.store.server.ha.core.monitor.FlowMonitor;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogTransfer;
import cn.coderule.minimq.store.server.ha.server.processor.SlaveOffsetReceiver;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionContext implements Serializable {
    private StoreConfig storeConfig;

    private LifecycleManager resourcePool;
    private ConnectionPool connectionPool;
    private WakeupCoordinator wakeupCoordinator;

    private FlowMonitor flowMonitor;
    private SlaveOffsetReceiver slaveOffsetReceiver;

    private CommitLogSynchronizer commitLogSynchronizer;
    private CommitLogTransfer commitLogTransfer;
    private CommitLogStore commitLogStore;

    private SocketChannel socketChannel;

    public static ConnectionContext of(HAContext haContext) {
        return ConnectionContext.builder()
            .storeConfig(haContext.getStoreConfig())
            .wakeupCoordinator(haContext.getWakeupCoordinator())
            .resourcePool(haContext.getResourcePool())
            .connectionPool(haContext.getConnectionPool())
            .build();
    }
}
