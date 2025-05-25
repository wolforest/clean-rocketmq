package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final String reviveTopic;
    private final long interval = 5_000;

    private volatile boolean serving = true;
    private volatile boolean master = true;
    private int scanTimes = 0;

    private final AckBuffer ackBuffer;

    public AckService(BrokerConfig brokerConfig, String reviveTopic) {
        this.brokerConfig = brokerConfig;
        this.reviveTopic = reviveTopic;

        this.ackBuffer = new AckBuffer();
    }

    @Override
    public String getServiceName() {
        return AckService.class.getSimpleName();
    }

    @Override
    public void run() {
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

    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {

    }

    public void ack(AckMsg ackMsg, int reviveQueueId) {

    }

    public long getLatestOffset(String topic, String group, int queueId) {
        return ackBuffer.getLatestOffset(KeyBuilder.buildConsumeKey(topic, group, queueId));
    }

    public int getTotalSize() {
        return ackBuffer.getTotalSize();
    }

    public int getBufferedSize() {
        return ackBuffer.getCount();
    }

}
