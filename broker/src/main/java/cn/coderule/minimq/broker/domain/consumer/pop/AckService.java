package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
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
    private final ConcurrentMap<String, PopCheckPointWrapper> checkPointMap;
    /**
     * topic@group@queueId -> queueWithTime
     */
    private final ConcurrentMap<String, QueueWithTime<PopCheckPointWrapper>> queueMap;

    private final List<Byte> ackIndexList;

    private volatile boolean serving = true;
    private volatile boolean master = true;
    private int scanTimes = 0;

    public AckService(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.reviveTopic = KeyBuilder.buildClusterReviveTopic(brokerConfig.getCluster());

        this.counter = new AtomicInteger(0);
        this.checkPointMap = new ConcurrentHashMap<>(16 * 1024);
        this.queueMap = new ConcurrentHashMap<>();
        this.ackIndexList = new ArrayList<>(32);
    }

    @Override
    public String getServiceName() {
        return AckService.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    public long getLatestOffset(String lockKey) {
        QueueWithTime<PopCheckPointWrapper> queue = this.queueMap.get(lockKey);
        if (queue == null) {
            return -1;
        }
        PopCheckPointWrapper pointWrapper = queue.get().peekLast();
        if (pointWrapper != null) {
            return pointWrapper.getNextBeginOffset();
        }
        return -1;
    }

    public long getLatestOffset(String topic, String group, int queueId) {
        return getLatestOffset(KeyBuilder.buildConsumeKey(topic, group, queueId));
    }
}
