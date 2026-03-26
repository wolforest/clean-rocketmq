package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import lombok.Getter;
import lombok.Setter;
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
    @Getter @Setter
    private volatile long dispatchedOffset = -1;

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
            this.dispatchedOffset = checkpointOffset;
            return;
        }

        this.dispatchedOffset = commitLog.getMinOffset();
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
                if (dispatchedOffset < 0) {
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

    private void listen() {

    }
}
