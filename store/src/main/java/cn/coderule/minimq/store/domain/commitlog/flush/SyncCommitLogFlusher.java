package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.cluster.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFileQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * simply sync commit log
 * for testing
 */
@Slf4j
public class SyncCommitLogFlusher implements CommitLogFlusher {
    private static final int RETRY_TIMES = 10;

    private final MappedFileQueue mappedFileQueue;

    public SyncCommitLogFlusher(MappedFileQueue mappedFileQueue) {
        this.mappedFileQueue = mappedFileQueue;
    }

    @Override
    public InsertFuture flush(InsertResult insertResult, MessageBO messageBO) {
        boolean status = forceFlush();
        if (!status) {
            return InsertFuture.failure();
        }

        return InsertFuture.success(insertResult, messageBO);
    }

    private boolean forceFlush() {
        boolean result = false;
        for (int i = 0; i < RETRY_TIMES && !result; i++) {
            result = mappedFileQueue.flush(0);
            String status = result ? "OK" : "Not OK";
            log.info("SyncCommitLogFlusher shutdown, retry {} times {}", i + 1, status);
        }

        return result;
    }
}
