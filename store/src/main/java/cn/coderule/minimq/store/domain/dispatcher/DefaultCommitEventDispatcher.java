package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.domain.model.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventHandler;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
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
    private volatile long dispatchedOffset = -1L;

    private final ArrayList<CommitEventHandler> consumerList = new ArrayList<>();

    private final CommitLog commitLog;

    public DefaultCommitEventDispatcher(CommitLog commitLog) {
        this.commitLog = commitLog;
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

    }
}
