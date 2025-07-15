package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.client.HAClient;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import cn.coderule.minimq.store.server.ha.server.HAServer;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import cn.coderule.minimq.store.server.ha.server.processor.SlaveOffsetReceiver;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import java.io.Serializable;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HAContext implements Serializable {
    private StoreConfig storeConfig;

    private HAServer haServer;
    @Builder.Default
    private HAClient haClient = null;

    private LifecycleManager resourcePool;
    private ConnectionPool connectionPool;
    private WakeupCoordinator wakeupCoordinator;

    private SlaveOffsetReceiver slaveOffsetReceiver;
    private CommitLogSynchronizer commitLogSynchronizer;

    private SocketAddress socketAddress;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
}
