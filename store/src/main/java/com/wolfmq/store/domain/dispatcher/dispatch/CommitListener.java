package com.wolfmq.store.domain.dispatcher.dispatch;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.store.domain.commitlog.file.FileCommitLog;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * Commit Event Listener
 * @renamed from CommitEventListener to CommitListener
 */
@Slf4j
public class CommitListener extends ServiceThread {
    private final CommitConfig commitConfig;
    private final CommitLog commitLog;
    private final DispatchQueue queue;
    private final QueueProgress queueProgress;

    private final int shardId;
    private final OffsetCodec offsetCodec;
    private final AtomicLong dispatchedOffset = new AtomicLong(-1L);

    public CommitListener(
        DispatchQueue queue,
        CommitLog commitLog,
        QueueProgress queueProgress,
        CommitConfig commitConfig
    ) {

        this.queue = queue;
        this.commitLog = commitLog;
        this.queueProgress = queueProgress;
        this.commitConfig = commitConfig;

        this.shardId = initShardId();
        offsetCodec = new OffsetCodec(this.shardId, commitConfig.getMaxShardingNumber());
        initDispatchedOffset();
    }



    @Override
    public String getServiceName() {
        return CommitListener.class.getSimpleName() + "-" + shardId;
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                if (!hasNewEvent()) {
                    await(commitConfig.getDispatchInterval());
                }

                this.listen();

                ThreadUtil.sleep(1);
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    public long getDispatchedOffset() {
        return dispatchedOffset.get();
    }

    public void setDispatchedOffset(long value) {
        this.dispatchedOffset.set(value);
    }

    public long increaseDispatchedOffset(long delta) {
        return this.dispatchedOffset.addAndGet(delta);
    }

    private int initShardId() {
        if (commitLog instanceof FileCommitLog) {
            return  ((FileCommitLog) commitLog).getShardId();
        }

        return 0;
    }

    private void initDispatchedOffset() {
        Long checkpointOffset = queueProgress.getDispatchedOffset(shardId);

        if (null != checkpointOffset && checkpointOffset > -1) {
            long offset = offsetCodec.decode(checkpointOffset);
            this.dispatchedOffset.set(offset);
            return;
        }

        this.dispatchedOffset.set(commitLog.getMinOffset());
    }

    private void listen() {
        MessageBO messageBO = commitLog.select(this.dispatchedOffset.get());
        if (!isValid(messageBO)) {
            return;
        }

        try {
            CommitEvent event = CommitEvent.of(messageBO, shardId);
            this.queue.offer(event);
        } catch (InterruptedException e) {
            log.info("{} offer CommitEvent exception. ", this.getServiceName(), e);
            return;
        }

        this.dispatchedOffset.addAndGet(messageBO.getMessageLength());
    }

    private boolean isValid(MessageBO messageBO) {
        if (null == messageBO) {
            return false;
        }

        if (isOverflow(messageBO.getMessageLength())) {
            return false;
        }

        return messageBO.isValid();
    }

    private boolean hasNewEvent() {
        if (dispatchedOffset.get() < 0) {
            return false;
        }

        long maxOffset = commitLog.getMaxOffset();
        return this.dispatchedOffset.get() < maxOffset;
    }

    private boolean isOverflow(int size) {
        return this.dispatchedOffset.get() + size > commitLog.getMaxOffset();
    }
}
