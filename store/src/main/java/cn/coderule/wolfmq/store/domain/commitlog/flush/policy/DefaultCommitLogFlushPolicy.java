package cn.coderule.wolfmq.store.domain.commitlog.flush.policy;

import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.store.domain.commitlog.flush.FlushWatcher;
import cn.coderule.wolfmq.store.domain.commitlog.flush.Flusher;
import cn.coderule.wolfmq.store.domain.commitlog.flush.GroupFlusher;
import cn.coderule.wolfmq.store.domain.commitlog.flush.IntervalCommitter;
import cn.coderule.wolfmq.store.domain.commitlog.flush.IntervalFlusher;
import cn.coderule.wolfmq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.core.enums.store.FlushType;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
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
public class DefaultCommitLogFlushPolicy implements CommitLogFlushPolicy {
    private final CommitConfig commitConfig;


    private final int shardId;
    private final Flusher committer;
    private final Flusher flusher;
    private final FlushWatcher flushWatcher;

    public DefaultCommitLogFlushPolicy(
        CommitConfig commitConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint
    ) {
        this(0, commitConfig, mappedFileQueue, checkPoint);
    }

    public DefaultCommitLogFlushPolicy(
        int shardId,
        CommitConfig commitConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint
    ) {
        this.shardId = shardId;
        this.commitConfig = commitConfig;

        this.flushWatcher = new FlushWatcher();
        this.flusher = initFlusher(mappedFileQueue, checkPoint);
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

    private Flusher initFlusher(MappedFileQueue mappedFileQueue, CheckPoint checkPoint) {
        if (FlushType.SYNC.equals(commitConfig.getFlushType())) {
            return new GroupFlusher(mappedFileQueue, checkPoint);
        }

        return new IntervalFlusher(commitConfig, mappedFileQueue, checkPoint);
    }

    private EnqueueFuture syncFlush(InsertResult insertResult, MessageBO messageBO) {
        flusher.setMaxOffset(insertResult.getWroteOffset());

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
        flusher.setMaxOffset(insertResult.getWroteOffset());

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
