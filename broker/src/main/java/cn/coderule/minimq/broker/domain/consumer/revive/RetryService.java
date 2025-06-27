package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.core.enums.message.TagType;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
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
    private final MQService mqService;
    private final TopicService topicService;
    private final ConsumeOffsetService consumeOffsetService;

    public RetryService(ReviveContext context) {
        this.storeConfig = context.getStoreConfig();
        this.mqService = context.getMqService();
        this.topicService = context.getTopicService();
        this.consumeOffsetService = context.getConsumeOffsetService();
    }

    public boolean retry(PopCheckPoint point, MessageBO message) {
        MessageBO retryMessage = createRetryMessage(point, message);
        initRetryTopic(retryMessage.getTopic());
        initConsumeOffset(retryMessage.getTopic(), point.getCId());

        EnqueueResult result = mqService.enqueue(retryMessage);
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
        if (topicService.exists(topicName)) {
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
        topicService.putTopic(retryTopic);
    }

    private void initConsumeOffset(String topicName, String groupName) {
        long offset = consumeOffsetService.getOffset(
            groupName,
            topicName,
            0
        );

        if (offset >= 0) {
            return;
        }

        consumeOffsetService.putOffset(
            groupName,
            topicName,
            0,
            0
        );
    }
}
