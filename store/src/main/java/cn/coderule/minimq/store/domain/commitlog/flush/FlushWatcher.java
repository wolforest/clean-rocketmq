package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
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
            long sleepTime = calculateSleepTime(request, now);
            if (!ThreadUtil.sleep(sleepTime)) {
                log.warn("An exception occurred while waiting for flushing disk. this may caused by shutdown");
                break;
            }
        }
    }

    private long calculateSleepTime(GroupCommitRequest request, long now) {
        long sleepTime = (request.getDeadLine() - now) / 1_000_000;
        return Math.min(10, sleepTime);
    }

}
