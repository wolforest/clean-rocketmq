package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviveThread extends ServiceThread {
    private final StoreConfig storeConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;
    private long reviveOffset;

    private final ConsumeOffsetService consumeOffsetService;
    private final MQService mqService;
    private TopicService topicService;
    private SubscriptionService subscriptionService;

    private ReviveConsumer reviveConsumer;
    private RetryService retryService;
    private Reviver reviver;

    private volatile boolean skipRevive = false;

    public ReviveThread(
        StoreConfig  storeConfig,
        String reviveTopic,
        int queueId,
        MQService mqService,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeConfig = storeConfig;
        this.messageConfig = storeConfig.getMessageConfig();
        this.reviveTopic = reviveTopic;
        this.queueId = queueId;

        this.mqService = mqService;
        this.consumeOffsetService = consumeOffsetService;
    }

    public void setSkipRevive(boolean skip) {
        this.skipRevive = skip;
        reviveConsumer.setSkipRevive(skip);
        reviver.setSkipRevive(skip);
    }

    @Override
    public String getServiceName() {
        return ReviveThread.class.getSimpleName();
    }

    @Override
    public void run() {
        int counter = 1;
        while (!this.isStopped()) {
            if (shouldSkip()) continue;

            initOffset();
            ReviveBuffer buffer = reviveConsumer.consume(reviveOffset);
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}",
                    reviveTopic, queueId);
                continue;
            }

            reviver.revive(buffer);
            resetOffset(buffer);
            counter = calculateAndWait(buffer, counter);
        }
    }

    private void initOffset() {
        log.info("start revive topic={}; reviveQueueId={}",
            reviveTopic, queueId);

        reviveOffset = consumeOffsetService.getOffset(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId
        );
    }

    private void resetOffset(ReviveBuffer buffer) {
        if (skipRevive) {
            return;
        }

        reviveOffset = buffer.getOffset();

        if (buffer.getOffset() <= buffer.getInitialOffset()) {
            return;
        }

        commitOffset(reviveOffset);
    }

    private void commitOffset(long offset) {
        consumeOffsetService.putOffset(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId,
            offset
        );
    }

    private int calculateAndWait(ReviveBuffer buffer, int counter) {
        return counter;
    }

    private boolean shouldSkip() {
        return skipRevive;
    }
}
