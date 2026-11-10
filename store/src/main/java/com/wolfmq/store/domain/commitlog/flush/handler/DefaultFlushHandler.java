package com.wolfmq.store.domain.commitlog.flush.handler;

import com.wolfmq.common.domain.messagequeue.dto.EnqueueStatus;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.store.domain.commitlog.flush.FlushWatcher;
import com.wolfmq.store.domain.commitlog.flush.flusher.Flusher;
import com.wolfmq.store.domain.commitlog.flush.flusher.GroupFlusher;
import com.wolfmq.store.domain.commitlog.flush.flusher.IntervalCommitter;
import com.wolfmq.store.domain.commitlog.flush.flusher.IntervalFlusher;
import com.wolfmq.store.domain.commitlog.vo.GroupCommitRequest;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.flush.FlushType;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueResult;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import java.util.concurrent.CompletableFuture;

/**
 * commitlog flush service
 *  - flush service: async/sync flusher
 *  - commit service: commit to cache
 *  - flush watcher: flush timeout watcher
 * functions:
 *  - flush
 *  - start/shutdown
 */
public class DefaultFlushHandler implements FlushHandler {
    private final CommitConfig commitConfig;

    private final Flusher committer;
    private final Flusher flusher;
    private final FlushWatcher flushWatcher;

    public DefaultFlushHandler(
        CommitConfig commitConfig,
        MappedFileQueue mappedFileQueue,
        QueueProgress queueProgress
    ) {
        this.commitConfig = commitConfig;

        this.flushWatcher = new FlushWatcher();
        this.flusher = initFlusher(mappedFileQueue, queueProgress);
        this.committer = new IntervalCommitter(commitConfig, mappedFileQueue, flusher);
    }

    @Override
    public EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO) {
        if (FlushType.SYNC.equals(commitConfig.getFlushType())) {
            return syncFlush(insertResult, messageBO);
        }

        return asyncFlush(insertResult, messageBO);
    }

    @Override
    public void start() throws Exception {
        this.flusher.start();
        this.flushWatcher.start();

        if (commitConfig.isEnableWriteCache()) {
            this.committer.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        this.flusher.shutdown();
        this.flushWatcher.shutdown();

        if (commitConfig.isEnableWriteCache()) {
            this.committer.shutdown();
        }
    }

    private Flusher initFlusher(MappedFileQueue mappedFileQueue, QueueProgress queueProgress) {
        if (FlushType.SYNC.equals(commitConfig.getFlushType())) {
            return new GroupFlusher(mappedFileQueue, queueProgress);
        }

        return new IntervalFlusher(commitConfig, mappedFileQueue, queueProgress);
    }

    private EnqueueFuture syncFlush(InsertResult insertResult, MessageBO messageBO) {
        flusher.setMaxOffset(messageBO.getCommitOffset());

        if (!messageBO.isWaitStore()) {
            flusher.wakeup();
            return EnqueueFuture.success(insertResult, messageBO);
        }

        GroupCommitRequest request = createGroupCommitRequest(insertResult);
        flusher.addRequest(request);
        flushWatcher.addRequest(request);

        return formatResult(insertResult, request, messageBO);
    }

    private EnqueueFuture asyncFlush(InsertResult insertResult, MessageBO messageBO) {
        flusher.setMaxOffset(messageBO.getCommitOffset());

        if (commitConfig.isEnableWriteCache()) {
            committer.wakeup();
        } else {
            flusher.wakeup();
        }

        return EnqueueFuture.success(insertResult, messageBO);
    }

    private EnqueueFuture formatResult(InsertResult insertResult, GroupCommitRequest request, MessageBO messageBO) {
        CompletableFuture<EnqueueResult> result = request.future()
            .thenApply(
                flushStatus -> buildEnqueueResult(flushStatus, insertResult, messageBO)
            );

        return EnqueueFuture.builder()
            .insertResult(insertResult)
            .future(result)
            .build();
    }

    private EnqueueResult buildEnqueueResult(EnqueueStatus status, InsertResult insertResult, MessageBO messageBO) {
        return EnqueueResult.builder()
            .status(status)
            .insertResult(insertResult)
            .storeGroup(messageBO.getStoreGroup())
            .messageId(messageBO.getUniqueKey())
            .transactionId(messageBO.getTransactionId())
            .commitOffset(messageBO.getCommitOffset())
            .queueId(messageBO.getQueueId())
            .queueOffset(messageBO.getQueueOffset())
            .build();
    }

    private GroupCommitRequest createGroupCommitRequest(InsertResult insertResult) {
        long nextOffset = insertResult.getWroteOffset() + insertResult.getWroteBytes();
        long deadLine = System.nanoTime() + commitConfig.getFlushTimeout() * 1_000_000L;

        return GroupCommitRequest.builder()
            .offset(insertResult.getWroteOffset())
            .nextOffset(nextOffset)
            .deadLine(deadLine)
            .build();
    }
}
