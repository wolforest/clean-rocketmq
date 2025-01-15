package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.common.util.lang.ThreadUtil;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.model.checkpoint.CheckPoint;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.utils.lock.CommitLogLock;
import com.wolf.minimq.domain.utils.lock.CommitLogSpinLock;
import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import java.util.LinkedList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupFlushService extends FlushService {
    private static final long FLUSH_JOIN_TIME = 5 * 60 * 1000;

    private volatile LinkedList<GroupCommitRequest> requestsWrite = new LinkedList<>();
    private volatile LinkedList<GroupCommitRequest> requestsRead = new LinkedList<>();
    private final CommitLogLock lock = new CommitLogSpinLock();
    private final MappedFileQueue mappedFileQueue;
    private final CheckPoint checkPoint;

    public GroupFlushService(MappedFileQueue mappedFileQueue, CheckPoint checkPoint) {
        this.mappedFileQueue = mappedFileQueue;
        this.checkPoint = checkPoint;
    }

    @Override
    public String getServiceName() {
        return GroupFlushService.class.getSimpleName();
    }

    @Override
    public long getJoinTime() {
        return FLUSH_JOIN_TIME;
    }

    public void addRequest(GroupCommitRequest request) {
        lock.lock();
        try {
            this.requestsWrite.addLast(request);
        } finally {
            lock.unlock();
        }
        this.wakeup();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            try {
                await(10);
                swapRequests();
                doCommit();
            } catch (Exception e) {
                log.warn("{} service has exception. ", this.getServiceName(), e);
            }
        }

        ThreadUtil.sleep(10, "GroupCommitService Exception, ");

        swapRequests();
        doCommit();
    }

    private void swapRequests() {
        lock.lock();
        try {
            LinkedList<GroupCommitRequest> tmp = this.requestsWrite;
            this.requestsWrite = this.requestsRead;
            this.requestsRead = tmp;
        } finally {
            lock.unlock();
        }
    }

    private void doCommit() {
        if (this.requestsRead.isEmpty()) {
            // Because of individual messages is set to not sync flush, it
            // will come to this process
            mappedFileQueue.flush(0);
            return;
        }

        long maxOffset = 0L;
        for (GroupCommitRequest req : this.requestsRead) {
            // There may be a message in the next file, so a maximum of
            // two times the flush
            boolean flushOK = mappedFileQueue.getFlushPosition() >= req.getNextOffset();
            for (int i = 0; i < 2 && !flushOK; i++) {
                mappedFileQueue.flush(0);
                flushOK = mappedFileQueue.getFlushPosition() >= req.getNextOffset();
            }

            req.wakeup(flushOK ? EnqueueStatus.PUT_OK : EnqueueStatus.FLUSH_DISK_TIMEOUT);
            if (!flushOK && req.getNextOffset() > maxOffset) {
                maxOffset = req.getNextOffset();
            }
        }
        // write check point
        checkPoint.getMaxOffset().setCommitLogOffset(maxOffset);

        this.requestsRead = new LinkedList<>();

    }


}
