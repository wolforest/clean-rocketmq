package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * Commit Event Listener
 * @renamed from CommitEventListener to CommitListener
 */
@Slf4j
public class CommitListener extends ServiceThread {
    private final CommitConfig config;

    private final CommitLog commitLog;
    private final DispatchQueue queue;
    private final CheckPoint checkPoint;

    private final int shardId;
    private final AtomicLong dispatchedOffset = new AtomicLong(-1L);

    public CommitListener(
        CommitConfig config,
        DispatchQueue queue,
        CommitLog commitLog,
        CheckPoint checkPoint
    ) {
        this.config = config;

        this.queue = queue;
        this.commitLog = commitLog;
        this.checkPoint = checkPoint;
        this.shardId = commitLog.getShardId();

        initDispatchedOffset();
    }

    private void initDispatchedOffset() {
        Long checkpointOffset = checkPoint.getMaxOffset().getDispatchedOffset(shardId);

        if (null != checkpointOffset && checkpointOffset > -1) {
            this.dispatchedOffset.set(checkpointOffset);;
            return;
        }

        this.dispatchedOffset.set(commitLog.getMinOffset());
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
                if (hasNewEvent()) {
                    return;
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

    private void listen() {
        MessageBO messageBO = commitLog.select(this.dispatchedOffset.get());
        if (isOverflow(messageBO.getMessageLength())) {
            return;
        }

        if (!messageBO.isValid()) {
            return;
        }

        try {
            CommitEvent event = CommitEvent.of(messageBO);
            this.queue.offer(event);
        } catch (InterruptedException e) {
            log.info("{} offer CommitEvent exception. ", this.getServiceName(), e);
            return;
        }

        this.dispatchedOffset.addAndGet(messageBO.getMessageLength());
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
