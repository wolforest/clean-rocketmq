package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveContext;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveObj;
import cn.coderule.minimq.domain.service.broker.infra.MQStore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviveThread extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;

    private final MQStore mqStore;

    private volatile boolean skipRevive = false;

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
        while (!this.isStopped()) {
            if (shouldSkip()) continue;

            log.info("start revive topic={}; reviveQueueId={}", reviveTopic, queueId);

            ReviveObj reviveObj = consumeReviveObj();
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}", reviveTopic, queueId);
                continue;
            }

            revive(reviveObj);
        }
    }

    private ReviveObj consumeReviveObj() {
        ReviveContext context = new ReviveContext();
        return null;
    }

    private void revive(ReviveObj reviveObj) {

    }

    private boolean shouldSkip() {
        return false;
    }
}
