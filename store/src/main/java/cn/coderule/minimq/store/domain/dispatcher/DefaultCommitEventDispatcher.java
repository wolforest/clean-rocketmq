package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventHandler;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCommitEventDispatcher extends ServiceThread  implements CommitEventDispatcher {
    /**
     * startOffset (confirmOffset in RocketMQ)
     *
     */
    @Getter @Setter
    private volatile long dispatchedOffset = -1;

    private final ArrayList<CommitEventHandler> consumerList = new ArrayList<>();

    private final StoreConfig storeConfig;
    private final CommitLog commitLog;
    private final CheckPoint checkPoint;

    public DefaultCommitEventDispatcher(StoreConfig storeConfig, CommitLog commitLog, CheckPoint checkPoint) {
        this.storeConfig = storeConfig;
        this.commitLog = commitLog;
        this.checkPoint = checkPoint;
    }

    @Override
    public void registerHandler(CommitEventHandler handler) {
        consumerList.add(handler);
    }

    @Override
    public String getServiceName() {
        return DefaultCommitEventDispatcher.class.getSimpleName();
    }

    @Override
    public void dispatch(CommitEvent event) {
        if (consumerList.isEmpty()) {
            return;
        }

        for (CommitEventHandler consumer : consumerList) {
            consumer.handle(event);
        }
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                ThreadUtil.sleep(1);
                this.loadAndDispatch();
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private void loadAndDispatch() {
        initDispatchedOffset();
        boolean success = true;

        while (success && hasNewEvent()) {
            MessageBO messageBO = commitLog.select(this.dispatchedOffset);
            if (messageBO == null) {
                log.error("invalid commitLog offset: {}", this.dispatchedOffset);
                break;
            }

            success = dispatch(messageBO);
            if (success) {
                saveDispatchedOffset(messageBO.getCommitOffset());
            }

        }
    }

    private void saveDispatchedOffset(long offset) {
        this.dispatchedOffset = offset;
        checkPoint.getMaxOffset().setDispatchedOffset(offset);
    }

    private boolean dispatch(MessageBO messageBO) {
        return true;
    }

    private void initDispatchedOffset() {
        if (this.dispatchedOffset > -1) {
            return;
        }

        long checkpointOffset = checkPoint.getMaxOffset().getDispatchedOffset();
        if (checkpointOffset > -1) {
            this.dispatchedOffset = checkpointOffset;
            return;
        }

        this.dispatchedOffset = commitLog.getMinOffset();
    }

    private boolean hasNewEvent() {
        return this.dispatchedOffset < commitLog.getMaxOffset();
    }
}
