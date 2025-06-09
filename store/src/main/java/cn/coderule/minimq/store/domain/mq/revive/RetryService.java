package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.enums.message.TagType;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryService {
    private final StoreConfig storeConfig;
    private final MQService mqService;
    private final TopicService topicService;
    private final ConsumeOffsetService consumeOffsetService;

    public RetryService(
        StoreConfig storeConfig,
        MQService mqService,
        TopicService topicService,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeConfig = storeConfig;
        this.mqService = mqService;
        this.topicService = topicService;
        this.consumeOffsetService = consumeOffsetService;
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

    }
}
