package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.model.consumer.pop.QueueWithTime;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AckService extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final String reviveTopic;
    private final long interval = 5_000;
    private final AtomicInteger counter;

    /**
     * mergeKey -> checkPointWrapper
     * mergeKey: topic + group + queueId + startOffset + popTime + brokerName
     */
    private final ConcurrentMap<String, PopCheckPointWrapper> buffer;
    /**
     * topic@group@queueId -> queueWithTime
     */
    private final ConcurrentMap<String, QueueWithTime<PopCheckPointWrapper>> commitOffsets;

    private final List<Byte> ackIndexList;

    private volatile boolean serving = true;
    private volatile boolean master = true;
    private int scanTimes = 0;

    private final AckBuffer ackBuffer;

    public AckService(BrokerConfig brokerConfig, String reviveTopic) {
        this.brokerConfig = brokerConfig;
        this.reviveTopic = reviveTopic;

        this.counter = new AtomicInteger(0);
        this.buffer = new ConcurrentHashMap<>(16 * 1024);
        this.commitOffsets = new ConcurrentHashMap<>();
        this.ackIndexList = new ArrayList<>(32);

        this.ackBuffer = new AckBuffer();
    }

    @Override
    public String getServiceName() {
        return AckService.class.getSimpleName();
    }

    @Override
    public void run() {

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
