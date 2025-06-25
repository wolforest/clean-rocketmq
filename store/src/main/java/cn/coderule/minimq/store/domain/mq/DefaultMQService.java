package cn.coderule.minimq.store.domain.mq;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.model.consumer.DequeueRequest;
import cn.coderule.minimq.domain.domain.model.consumer.DequeueResult;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.domain.domain.model.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.store.domain.mq.queue.DequeueService;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.domain.mq.queue.MessageService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQService implements MQService {

    private final EnqueueService enqueueService;
    private final DequeueService dequeueService;
    private final MessageService messageService;

    public DefaultMQService(
        EnqueueService enqueueService,
        DequeueService dequeueService,
        MessageService messageService
    ) {
        this.enqueueService = enqueueService;
        this.dequeueService = dequeueService;
        this.messageService = messageService;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return enqueueService.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return enqueueService.enqueueAsync(messageBO);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        return dequeueService.dequeueAsync(group, topic, queueId, num);
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        return dequeueService.dequeue(group, topic, queueId, num);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        return get(topic, queueId, offset, 1);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        return messageService.get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        return messageService.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        List<MessageBO> messageList = getMessage(topic, queueId, offset, 1);

        return CollectionUtil.isEmpty(messageList)
            ? null
            : messageList.get(0);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        DequeueResult result = get(topic, queueId, offset, num);
        return result.getMessageList();
    }

}
