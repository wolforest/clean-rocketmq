package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.common.lang.concurrent.ServiceThread;
import com.wolf.common.util.lang.ThreadUtil;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlushWatcher extends ServiceThread {
    private final LinkedBlockingQueue<GroupCommitRequest> commitRequests = new LinkedBlockingQueue<>();

    @Override
    public String getServiceName() {
        return FlushWatcher.class.getSimpleName();
    }

    public void addRequest(GroupCommitRequest request) {
        commitRequests.add(request);
    }

    @Override
    public void run() {
        while (!isStopped()) {
            GroupCommitRequest request = takeRequest();
            if (request == null) {
                continue;
            }
            monitorRequest(request);
        }

    }

    private GroupCommitRequest takeRequest() {
        try {
            return commitRequests.take();
        } catch (InterruptedException e) {
            log.error("FlushWatcher takeRequest error", e);
        }

        return null;
    }

    private void monitorRequest(@NonNull GroupCommitRequest request) {
        while (!request.future().isDone()) {
            long now = System.nanoTime();
            if (now - request.getDeadLine() >= 0) {
                request.wakeup(EnqueueStatus.FLUSH_DISK_TIMEOUT);
                break;
            }

            // To avoid frequent thread switching, replace future.get with sleep here,
            long sleepTime = (request.getDeadLine() - now) / 1_000_000;
            sleepTime = Math.min(10, sleepTime);
            if (sleepTime == 0) {
                request.wakeup(EnqueueStatus.FLUSH_DISK_TIMEOUT);
                break;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.warn("An exception occurred while waiting for flushing disk to complete. this may caused by shutdown");
                break;
            }
        }
    }


}
