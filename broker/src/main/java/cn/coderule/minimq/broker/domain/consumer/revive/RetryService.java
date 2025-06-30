package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.core.enums.message.TagType;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.service.broker.infra.MQFacade;
import cn.coderule.minimq.domain.service.broker.infra.meta.ConsumeOffsetFacade;
import cn.coderule.minimq.domain.service.broker.infra.meta.TopicFacade;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * stateless retry service
 *  - create retry message from checkpoint and checkpoint related message
 *  - create retry topic if not exists
 *  - init consume offset if not exists
 *  - enqueue retry message
 */
@Slf4j
public class RetryService {
    private final StoreConfig storeConfig;

    private final MQFacade mqFacade;
    private final TopicFacade topicFacade;
    private final ConsumeOffsetFacade consumeOffsetFacade;

    public RetryService(ReviveContext context) {
        this.storeConfig = context.getStoreConfig();

        this.mqFacade = context.getMqFacade();
        this.topicFacade = context.getTopicFacade();
        this.consumeOffsetFacade = context.getConsumeOffsetFacade();
    }

    public boolean retry(PopCheckPoint point, MessageBO message) {
        MessageBO retryMessage = createRetryMessage(point, message);
        initRetryTopic(retryMessage.getTopic());
        initConsumeOffset(retryMessage.getTopic(), point.getCId());

        EnqueueRequest request = EnqueueRequest.create(retryMessage);
        EnqueueResult result = mqFacade.enqueue(request);
        if (!result.isSuccess()) {
            log.error("Retry failed, retryMessage: {}, result: {}", retryMessage, result);
        }

        return result.isSuccess();
    }

    private MessageBO createRetryMessage(PopCheckPoint point, MessageBO message) {
        SocketAddress storeHost = new InetSocketAddress(storeConfig.getHost(), storeConfig.getPort());
        return PopConverter.toMessageBO(point, message, storeHost);
    }

    private void initRetryTopic(String topicName) {
        if (topicFacade.exists(topicName)) {
            return;
        }

        Topic retryTopic = Topic.builder()
            .topicName(topicName)
            .readQueueNums(PopConstants.retryQueueNum)
            .writeQueueNums(PopConstants.retryQueueNum)
            .tagType(TagType.SINGLE_TAG)
            .perm(6)
            .topicSysFlag(0)
            .build();
        topicFacade.saveTopic(TopicRequest.build(retryTopic));
    }

    private void initConsumeOffset(String topicName, String groupName) {
        OffsetRequest getRequest = OffsetRequest.builder()
            .consumerGroup(groupName)
            .topicName(topicName)
            .queueId(0)
            .build();

        long offset = consumeOffsetFacade
            .getOffset(getRequest)
            .getOffset();

        if (offset >= 0) {
            return;
        }

        OffsetRequest putRequest = OffsetRequest.builder()
            .consumerGroup(groupName)
            .topicName(topicName)
            .queueId(0)
            .newOffset(0)
            .build();
        consumeOffsetFacade.putOffset(putRequest);
    }
}
