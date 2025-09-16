package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntervalCommitter extends Flusher {
    private final CommitConfig config;
    private final MappedFileQueue mappedFileQueue;
    private final Flusher flusher;

    private long lastCommitTime = 0;

    public IntervalCommitter(
        CommitConfig config,
        MappedFileQueue mappedFileQueue,
        Flusher flusher) {

        this.config = config;
        this.mappedFileQueue = mappedFileQueue;
        this.flusher = flusher;
    }

    @Override
    public String getServiceName() {
        return IntervalCommitter.class.getSimpleName();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            commit();
        }
        forceCommit();
    }

    private void commit() {
        int minCommitPages = getMinCommitPages();
        try {
            boolean result = mappedFileQueue.commit(minCommitPages);
            if (!result) {
                flusher.wakeup();
            }

            await(config.getCommitInterval());
        } catch (Throwable e) {
            log.error("{} Commit error", getServiceName(), e);
        }
    }

    private int getMinCommitPages() {
        int minPages = config.getMinCommitPages();
        long now = System.currentTimeMillis();
        if (now - lastCommitTime >= config.getThroughCommitInterval()) {
            lastCommitTime = now;
            minPages = 0;
        }

        return minPages;
    }

    private void forceCommit() {
        boolean result = false;
        for (int i = 0; i < RETRY_TIMES && !result; i++) {
            result = mappedFileQueue.commit(0);

            String status = result ? "OK" : "Not OK";
            log.info("{} service shutdown, retry {} times {}",
                this.getServiceName(), i + 1, status);
        }
    }

}
