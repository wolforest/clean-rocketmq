package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.service.broker.infra.MQStore;

public class ReviveThread extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;

    private final MQStore mqStore;

    public ReviveThread(BrokerConfig brokerConfig, String reviveTopic, int queueId, MQStore mqStore) {
        this.brokerConfig = brokerConfig;
        this.messageConfig = brokerConfig.getMessageConfig();
        this.reviveTopic = reviveTopic;
        this.queueId = queueId;
        this.mqStore = mqStore;
    }

    @Override
    public String getServiceName() {
        return ReviveThread.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
