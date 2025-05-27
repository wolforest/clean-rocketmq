package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;

public class ReviveThread extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;

    public ReviveThread(BrokerConfig brokerConfig, String reviveTopic, int queueId) {
        this.brokerConfig = brokerConfig;
        this.messageConfig = brokerConfig.getMessageConfig();
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
