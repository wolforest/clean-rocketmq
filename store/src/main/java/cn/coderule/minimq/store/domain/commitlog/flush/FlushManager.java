package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.CommitLogConfig;
import cn.coderule.minimq.domain.enums.FlushType;
import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.model.dto.InsertFuture;
import cn.coderule.minimq.domain.model.dto.InsertResult;
import cn.coderule.minimq.domain.model.bo.MessageBO;
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

    private final CommitLogConfig commitLogConfig;

    private final Flusher commitService;
    private final Flusher flusher;
    private final FlushWatcher flushWatcher;

    public FlushManager(
        CommitLogConfig commitLogConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint) {
        this.commitLogConfig = commitLogConfig;

        this.flushWatcher = new FlushWatcher();

        if (FlushType.SYNC.equals(commitLogConfig.getFlushType())) {
            this.flusher = new GroupFlusher(mappedFileQueue, checkPoint);
        } else {
            this.flusher = new IntervalFlusher(commitLogConfig, mappedFileQueue, checkPoint);
        }

        this.commitService = new IntervalCommitter(commitLogConfig, mappedFileQueue, flusher);
    }

    public InsertFuture flush(InsertResult insertResult, MessageBO messageBO) {
        if (FlushType.SYNC.equals(commitLogConfig.getFlushType())) {
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

        if (commitLogConfig.isEnableWriteCache()) {
            commitService.wakeup();
        } else {
            flusher.wakeup();
        }

        return InsertFuture.success(insertResult);
    }

    private InsertFuture formatResult(InsertResult insertResult, GroupCommitRequest request) {
        CompletableFuture<EnqueueResult> result = request.future()
            .thenApplyAsync(
                flushStatus -> EnqueueResult.builder()
                    .status(flushStatus)
                    .insertResult(insertResult)
                    .build()
            );

        return InsertFuture.builder()
            .insertResult(insertResult)
            .future(result)
            .build();
    }

    private GroupCommitRequest createGroupCommitRequest(InsertResult insertResult) {
        long nextOffset = insertResult.getWroteOffset() + insertResult.getWroteBytes();
        long deadLine = System.nanoTime() + commitLogConfig.getFlushTimeout();

        return GroupCommitRequest.builder()
            .offset(insertResult.getWroteOffset())
            .nextOffset(nextOffset)
            .deadLine(deadLine)
            .build();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
        this.state = State.STARTING;

        this.flusher.start();

        this.flushWatcher.setDaemon(true);
        this.flushWatcher.start();

        if (commitLogConfig.isEnableWriteCache()) {
            this.commitService.start();
        }

        this.state = State.RUNNING;
    }

    @Override
    public void shutdown() {
        this.state = State.SHUTTING_DOWN;

        this.flusher.shutdown();
        this.flushWatcher.shutdown();

        if (commitLogConfig.isEnableWriteCache()) {
            this.commitService.shutdown();
        }

        this.state = State.TERMINATED;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return state;
    }
}
