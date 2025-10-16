package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.core.enums.store.FlushType;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
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
public class DefaultCommitLogFlusher implements CommitLogFlusher, Lifecycle {
    private final CommitConfig commitConfig;

    private final Flusher commitService;
    private final Flusher flusher;
    private final FlushWatcher flushWatcher;

    public DefaultCommitLogFlusher(
        CommitConfig commitConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint
    ) {
        this.commitConfig = commitConfig;

        this.flushWatcher = new FlushWatcher();
        this.flusher = initFlusher(mappedFileQueue, checkPoint);
        this.commitService = new IntervalCommitter(commitConfig, mappedFileQueue, flusher);
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
            this.commitService.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        this.flusher.shutdown();
        this.flushWatcher.shutdown();

        if (commitConfig.isEnableWriteCache()) {
            this.commitService.shutdown();
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
            commitService.wakeup();
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
