package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.config.CommitLogConfig;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntervalFlusher extends Flusher {
    private static final long FLUSH_JOIN_TIME = 5 * 60 * 1000;

    private final CommitLogConfig commitLogConfig;
    private final MappedFileQueue mappedFileQueue;
    private final CheckPoint checkPoint;

    private long lastFlushTime = 0;

    public IntervalFlusher(
        CommitLogConfig commitLogConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint) {

        this.commitLogConfig = commitLogConfig;
        this.mappedFileQueue = mappedFileQueue;
        this.checkPoint = checkPoint;
    }

    @Override
    public String getServiceName() {
        return IntervalFlusher.class.getSimpleName();
    }

    @Override
    public long getJoinTime() {
        return FLUSH_JOIN_TIME;
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            flush();
        }

        // ensure flush before exit
        forceFlush();
    }

    private void flush() {
        try {
            sleepOrWait();

            int minFlushPages = getMinFlushPages();
            mappedFileQueue.flush(minFlushPages);

            if (0 == minFlushPages && maxOffset > 0) {
                checkPoint.getMaxOffset().setCommitLogOffset(maxOffset);
            }
        } catch (Exception e) {
            log.warn("{} service run flush error", getServiceName(), e);
        }
    }

    private void sleepOrWait() throws InterruptedException {
        int interval = commitLogConfig.getFlushInterval();
        if (commitLogConfig.isEnableFlushSleep()) {
            Thread.sleep(interval);
        } else {
            await(interval);
        }
    }

    private int getMinFlushPages() {
        int minFlushPages = commitLogConfig.getMinFlushPages();

        long now = System.currentTimeMillis();
        if (now - lastFlushTime >= commitLogConfig.getThroughFlushInterval()) {
            lastFlushTime = now;
            minFlushPages = 0;
        }

        return minFlushPages;
    }

    private void forceFlush() {
        boolean result = false;
        for (int i = 0; i < RETRY_TIMES && !result; i++) {
            result = mappedFileQueue.flush(0);
            String status = result ? "OK" : "Not OK";
            log.info("{} service shutdown, retry {} times {}", getServiceName(), i + 1, status);
        }

        checkPoint.getMaxOffset().setCommitLogOffset(maxOffset);
    }

}
