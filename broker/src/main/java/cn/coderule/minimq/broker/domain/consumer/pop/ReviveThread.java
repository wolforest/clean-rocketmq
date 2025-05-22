package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;

public class ReviveThread extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final String reviveTopic;
    private final int queueId;

    public ReviveThread(BrokerConfig brokerConfig, String reviveTopic, int queueId) {
        this.brokerConfig = brokerConfig;
        this.reviveTopic = reviveTopic;
        this.queueId = queueId;
    }

    @Override
    public String getServiceName() {
        return ReviveThread.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
