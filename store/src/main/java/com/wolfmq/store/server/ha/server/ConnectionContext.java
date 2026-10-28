package com.wolfmq.store.server.ha.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.store.server.ha.core.HAContext;
import com.wolfmq.store.server.ha.core.monitor.FlowMonitor;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import com.wolfmq.store.server.ha.server.processor.CommitLogTransfer;
import com.wolfmq.store.server.ha.server.processor.SlaveOffsetReceiver;
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
