package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.lock.commitlog.CommitLogLock;
import cn.coderule.minimq.domain.core.lock.commitlog.CommitLogSpinLock;
import cn.coderule.minimq.domain.domain.cluster.store.GroupCommitEvent;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogSynchronizer extends ServiceThread implements Lifecycle {
    private StoreConfig storeConfig;
    private CommitLogStore commitLogStore;
    private WakeupCoordinator wakeupCoordinator;

    private final CommitLogLock commitLogLock;

    private volatile List<GroupCommitEvent> writeRequests;
    private volatile List<GroupCommitEvent> readRequests;


    public CommitLogSynchronizer() {
        this.commitLogLock = new CommitLogSpinLock();
        this.readRequests = new LinkedList<>();
        this.writeRequests = new LinkedList<>();
    }

    @Override
    public String getServiceName() {
        return CommitLogSynchronizer.class.getSimpleName();
    }

    public CompletableFuture<EnqueueResult> sync(InsertFuture request) {
        long timeout = storeConfig.getSlaveTimeout();
        GroupCommitEvent event = new GroupCommitEvent(request, timeout);

        commitLogLock.lock();
        try {
            this.writeRequests.add(event);
        } finally {
            commitLogLock.unlock();
        }

        wakeup();

        return null;
    }

    @Override
    public void run() {

    }
}
