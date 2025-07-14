package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.core.lock.commitlog.CommitLogLock;
import cn.coderule.minimq.domain.core.lock.commitlog.CommitLogSpinLock;
import cn.coderule.minimq.domain.domain.cluster.store.GroupCommitEvent;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogSynchronizer extends ServiceThread implements Lifecycle {
    private StoreConfig storeConfig;
    private CommitLogStore commitLogStore;
    private WakeupCoordinator wakeupCoordinator;
    private SlaveMonitor slaveMonitor;

    private final CommitLogLock lock;

    private volatile List<GroupCommitEvent> writeRequests;
    private volatile List<GroupCommitEvent> readRequests;


    public CommitLogSynchronizer() {
        this.lock = new CommitLogSpinLock();
        this.readRequests = new LinkedList<>();
        this.writeRequests = new LinkedList<>();
    }

    @Override
    public String getServiceName() {
        return CommitLogSynchronizer.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                this.await(10);
                this.swapRequests();
                this.waitTransfer();
            } catch (Exception e) {
                log.warn("{} occurs exception.", this.getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    public CompletableFuture<EnqueueResult> sync(InsertFuture request) {
        long timeout = storeConfig.getSlaveTimeout();
        GroupCommitEvent event = new GroupCommitEvent(request, timeout);

        lock.lock();
        try {
            this.writeRequests.add(event);
        } finally {
            lock.unlock();
        }

        wakeup();

        return null;
    }

    public void wakeupCoordinator() {
        wakeupCoordinator.wakeup();
    }

    private void waitTransfer() {
        if (this.readRequests.isEmpty()) {
            return;
        }

        for (GroupCommitEvent event : this.readRequests) {
            boolean allDone = event.getAckNums() == MQConstants.ALL_ACK_IN_SYNC_STATE_SET;
            boolean transferDone = waitTransfer(event, allDone);

            if (!transferDone) {
                log.warn("wait transfer timeout, offset: {}, request ack: {}",
                    event.getNextOffset(), event.getAckNums());
            }

            EnqueueStatus status = transferDone
                ? EnqueueStatus.PUT_OK
                : EnqueueStatus.FLUSH_SLAVE_TIMEOUT;
            event.wakeupCustomer(status);
        }
    }

    private boolean waitTransfer(GroupCommitEvent event, boolean allDone) {
        boolean transferDone = false;

        for (int i=0; !transferDone && event.getDeadLine() - System.nanoTime() > 0; i++) {
            if (i > 0) {
                wakeupCoordinator.await(1);
            }

            if (!allDone && event.getAckNums() <= 1) {
                transferDone = slaveMonitor.getAckOffset() >= event.getNextOffset();
                continue;
            }

            transferDone = waitTransfer(event);
        }

        return transferDone;
    }

    private boolean waitTransfer(GroupCommitEvent event) {
        boolean transferDone = false;


        return transferDone;
    }

    private void swapRequests() {
        lock.lock();
        try {
            List<GroupCommitEvent> tmp = this.writeRequests;
            this.writeRequests = this.readRequests;
            this.readRequests = tmp;
        } finally {
            lock.unlock();
        }
    }


}
