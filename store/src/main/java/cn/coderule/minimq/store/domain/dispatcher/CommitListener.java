package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
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
                ThreadUtil.sleep(1);
                this.listen();
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private void listen() {

    }
}
