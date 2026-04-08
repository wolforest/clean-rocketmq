package cn.coderule.wolfmq.broker.domain.transaction.check.service;

import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscardService {
    private final BrokerConfig brokerConfig;
    private final TransactionMessageService messageService;

    public DiscardService(BrokerConfig brokerConfig, TransactionMessageService messageService) {
        this.brokerConfig = brokerConfig;
        this.messageService = messageService;
    }

    public void discard(MessageBO prepareMessage) {
        try {
            Topic topic = messageService.getOrCreateDiscardTopic();
            int queueId = getQueueId();

            MessageBO discardMessage = createDiscardMessage(prepareMessage, topic, queueId);
            EnqueueResult result = messageService.enqueueMessage(discardMessage);

            if (!result.isSuccess()) {
                log.error("[Transaction] Discard message error: {}", result);
            }
        } catch (Exception e) {
            log.error("[Transaction] Discard message error", e);
        }
    }

    private int getQueueId() {
        int queueNum = brokerConfig.getTopicConfig().getDiscardQueueNum();
        return ThreadLocalRandom.current().nextInt(99999999) % queueNum;
    }

    private MessageBO createDiscardMessage(MessageBO prepareMessage, Topic topic, int queueId) {
        MessageBO discardMessage = MessageBO.builder()
            .storeGroup(prepareMessage.getStoreGroup())
            .topic(topic.getTopicName())
            .queueId(queueId)
            .body(prepareMessage.getBody())
            .flag(prepareMessage.getFlag())
            .tagsCode(prepareMessage.getTagsCode())
            .sysFlag(prepareMessage.getSysFlag())
            .bornHost(prepareMessage.getBornHost())
            .bornTimestamp(prepareMessage.getBornTimestamp())
            .storeHost(prepareMessage.getStoreHost())
            .storeTimestamp(prepareMessage.getStoreTimestamp())
            .reconsumeTimes(prepareMessage.getReconsumeTimes())
            .messageId(prepareMessage.getMessageId())
            .properties(prepareMessage.getProperties())
            .build();

        discardMessage.setWaitStore(false);
        return discardMessage;
    }


}
