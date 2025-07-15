package cn.coderule.minimq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.store.server.ha.client.HAClient;
import cn.coderule.minimq.store.server.ha.core.HAContext;
import cn.coderule.minimq.store.server.ha.core.monitor.StateMonitor;
import cn.coderule.minimq.store.server.ha.core.monitor.StateRequest;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import cn.coderule.minimq.store.server.ha.server.HAServer;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HAService implements Lifecycle {
    private final HAServer haServer;
    private final HAClient haClient;

    private final ConnectionPool connectionPool;
    private final StateMonitor stateMonitor;
    private final CommitLogSynchronizer commitLogSynchronizer;

    public HAService(HAContext context) {
        this.haServer = context.getHaServer();
        this.haClient = context.getHaClient();
        this.connectionPool = context.getConnectionPool();
        this.commitLogSynchronizer = context.getCommitLogSynchronizer();

        this.stateMonitor = new StateMonitor(
            context.getStoreConfig(),
            context.getHaServer(),
            context.getHaClient()
        );
    }

    @Override
    public void start() throws Exception {
        this.stateMonitor.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.stateMonitor.stop();
    }

    public int countHealthySlave(long masterOffset) {
        return connectionPool.countHealthyConnection(masterOffset);
    }

    public void updateMasterAddress(String addr) {
        if (haClient == null) {
            return;
        }

        haClient.setMasterAddress(addr);
    }

    public void updateMasterHaAddress(String addr) {
        if (haClient == null) {
            return;
        }

        haClient.setMasterHaAddress(addr);
    }

    public CompletableFuture<EnqueueResult> syncCommitLog(InsertFuture result) {
        return commitLogSynchronizer.sync(result);
    }

    public void wakeupHAClient() {
        if (haClient == null) {
            return;
        }

        haClient.wakeup();
    }

    public void monitorState(StateRequest request) {
        this.stateMonitor.setRequest(request);
    }


}
