package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveContext;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveMap;
import cn.coderule.minimq.domain.service.broker.infra.MQStore;
import java.util.ArrayList;
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

            ReviveMap reviveMap = consumeReviveObj();
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}", reviveTopic, queueId);
                continue;
            }

            revive(reviveMap);
        }
    }

    private ReviveMap consumeReviveObj() {
        ReviveContext context = new ReviveContext();
        return null;
    }

    private void revive(ReviveMap reviveMap) {
        ArrayList<PopCheckPoint> checkPointList = reviveMap.getSortedList();
    }

    private boolean shouldSkip() {
        return false;
    }
}
