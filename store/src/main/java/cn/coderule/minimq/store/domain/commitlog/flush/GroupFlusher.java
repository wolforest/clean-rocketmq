package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.domain.enums.EnqueueStatus;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.utils.lock.CommitLogLock;
import cn.coderule.minimq.domain.utils.lock.CommitLogSpinLock;
import java.util.LinkedList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupFlusher extends Flusher {
    private static final long FLUSH_JOIN_TIME = 5 * 60 * 1000;

    private final MappedFileQueue mappedFileQueue;
    private final CheckPoint checkPoint;

    private volatile LinkedList<GroupCommitRequest> requestsWrite = new LinkedList<>();
    private volatile LinkedList<GroupCommitRequest> requestsRead = new LinkedList<>();

    private final CommitLogLock lock = new CommitLogSpinLock();

    public GroupFlusher(MappedFileQueue mappedFileQueue, CheckPoint checkPoint) {
        this.mappedFileQueue = mappedFileQueue;
        this.checkPoint = checkPoint;
    }

    @Override
    public String getServiceName() {
        return GroupFlusher.class.getSimpleName();
    }

    @Override
    public long getJoinTime() {
        return FLUSH_JOIN_TIME;
    }

    @Override
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
                flush();
            } catch (Exception e) {
                log.warn("{} service has exception. ", this.getServiceName(), e);
            }
        }

        ThreadUtil.sleep(10, "GroupCommitService Exception, ");

        swapRequests();
        flush();
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

    private void forceFlush() {
        // Because of individual messages is set to not sync flush, it
        // will come to this process
        mappedFileQueue.flush(0);
        checkPoint.getMaxOffset().setCommitLogOffset(maxOffset);
    }

    private void flush() {
        if (this.requestsRead.isEmpty()) {
            forceFlush();
            return;
        }

        long tmpOffset = this.maxOffset;
        for (GroupCommitRequest req : this.requestsRead) {
            boolean flushOK = flush(req);
            wakeupRequest(req, flushOK);

            if (flushOK && req.getOffset() > tmpOffset) {
                tmpOffset = req.getOffset();
            }
        }

        // write check point
        checkPoint.getMaxOffset().setCommitLogOffset(tmpOffset);
        this.requestsRead = new LinkedList<>();
    }

    private boolean flush(GroupCommitRequest request) {
        boolean flushOK = mappedFileQueue.getFlushPosition() >= request.getNextOffset();

        // There may be a message in the next file, so a maximum of
        // two times the flush
        for (int i = 0; i < 2 && !flushOK; i++) {
            mappedFileQueue.flush(0);
            flushOK = mappedFileQueue.getFlushPosition() >= request.getNextOffset();
        }

        return flushOK;
    }

    private void wakeupRequest(GroupCommitRequest request, boolean flushOK) {
        EnqueueStatus status = flushOK
            ? EnqueueStatus.PUT_OK :
            EnqueueStatus.FLUSH_DISK_TIMEOUT;

        request.wakeup(status);
    }

}
