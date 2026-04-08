package cn.coderule.wolfmq.store.domain.mq.ack;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckBuffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckMerger extends ServiceThread {
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final AckBuffer ackBuffer;

    private volatile boolean serving = true;
    private volatile boolean master = true;
    private int scanTimes = 0;

    public AckMerger(MessageConfig messageConfig, String reviveTopic, AckBuffer ackBuffer) {
        this.messageConfig = messageConfig;
        this.reviveTopic = reviveTopic;
        this.ackBuffer = ackBuffer;
    }

    @Override
    public String getServiceName() {
        return AckMerger.class.getSimpleName();
    }

    @Override
    public void run() {
        if (!messageConfig.isEnablePopBufferMerge()) {
            return;
        }

        while (!this.isStopped()) {
            try {
                scan();
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
                this.await(3_000);
            }
        }
    }

    private void scan() {
    }
}
