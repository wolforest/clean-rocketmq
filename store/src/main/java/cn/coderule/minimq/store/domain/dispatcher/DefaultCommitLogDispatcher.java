package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.domain.model.CommitLogEvent;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.CommitLogHandler;
import cn.coderule.minimq.domain.service.store.domain.CommitLogDispatcher;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCommitLogDispatcher extends ServiceThread  implements CommitLogDispatcher {
    /**
     * startOffset (confirmOffset in RocketMQ)
     *
     */
    @Getter @Setter
    private volatile long dispatchedOffset = -1L;

    private final ArrayList<CommitLogHandler> consumerList = new ArrayList<>();

    private final CommitLog commitLog;

    public DefaultCommitLogDispatcher(CommitLog commitLog) {
        this.commitLog = commitLog;
    }

    @Override
    public void registerHandler(CommitLogHandler handler) {
        consumerList.addLast(handler);
    }

    @Override
    public String getServiceName() {
        return DefaultCommitLogDispatcher.class.getSimpleName();
    }

    @Override
    public void dispatch(CommitLogEvent event) {
        if (consumerList.isEmpty()) {
            return;
        }

        for (CommitLogHandler consumer : consumerList) {
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
