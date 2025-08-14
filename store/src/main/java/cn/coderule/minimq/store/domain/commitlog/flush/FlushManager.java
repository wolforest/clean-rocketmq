package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.core.enums.store.FlushType;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.concurrent.CompletableFuture;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - StoreCheckPoint
 */
public class FlushManager implements Lifecycle {
    private State state = State.RUNNING;
    private final CommitConfig commitConfig;

    private final Flusher commitService;
    private final Flusher flusher;
    private final FlushWatcher flushWatcher;

    public FlushManager(
        CommitConfig commitConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint) {
        this.commitConfig = commitConfig;

        this.flushWatcher = new FlushWatcher();

        if (FlushType.SYNC.equals(commitConfig.getFlushType())) {
            this.flusher = new GroupFlusher(mappedFileQueue, checkPoint);
        } else {
            this.flusher = new IntervalFlusher(commitConfig, mappedFileQueue, checkPoint);
        }

        this.commitService = new IntervalCommitter(commitConfig, mappedFileQueue, flusher);
    }

    @Override
    public void start() throws Exception {
        this.state = State.STARTING;

        this.flusher.start();

        this.flushWatcher.setDaemon(true);
        this.flushWatcher.start();

        if (commitConfig.isEnableWriteCache()) {
            this.commitService.start();
        }

        this.state = State.RUNNING;
    }

    @Override
    public void shutdown() throws Exception {
        this.state = State.SHUTTING_DOWN;

        this.flusher.shutdown();
        this.flushWatcher.shutdown();

        if (commitConfig.isEnableWriteCache()) {
            this.commitService.shutdown();
        }

        this.state = State.TERMINATED;
    }

    public InsertFuture flush(InsertResult insertResult, MessageBO messageBO) {
        if (FlushType.SYNC.equals(commitConfig.getFlushType())) {
            return syncFlush(insertResult, messageBO);
        }

        return asyncFlush(insertResult);
    }

    private InsertFuture syncFlush(InsertResult insertResult, MessageBO messageBO) {
        flusher.setMaxOffset(insertResult.getWroteOffset());

        if (!messageBO.isWaitStore()) {
            flusher.wakeup();
            return InsertFuture.success(insertResult);
        }

        GroupCommitRequest request = createGroupCommitRequest(insertResult);
        flusher.addRequest(request);
        flushWatcher.addRequest(request);

        return formatResult(insertResult, request);
    }

    private InsertFuture asyncFlush(InsertResult insertResult) {
        flusher.setMaxOffset(insertResult.getWroteOffset());

        if (commitConfig.isEnableWriteCache()) {
            commitService.wakeup();
        } else {
            flusher.wakeup();
        }

        return InsertFuture.success(insertResult);
    }

    private InsertFuture formatResult(InsertResult insertResult, GroupCommitRequest request) {
        CompletableFuture<EnqueueResult> result = request.future()
            .thenApply(
                flushStatus -> buildEnqueueResult(flushStatus, insertResult)
            );

        return InsertFuture.builder()
            .insertResult(insertResult)
            .future(result)
            .build();
    }

    private EnqueueResult buildEnqueueResult(EnqueueStatus status, InsertResult insertResult) {
        return EnqueueResult.builder()
            .status(status)
            .insertResult(insertResult)
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
