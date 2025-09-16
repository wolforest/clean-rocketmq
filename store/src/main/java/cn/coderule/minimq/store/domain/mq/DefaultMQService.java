package cn.coderule.minimq.store.domain.mq;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.MQService;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
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
    public DequeueResult dequeue(DequeueRequest request) {
        return dequeueService.dequeue(request);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        return dequeueService.dequeueAsync(request);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        return messageService.get(topic, queueId, offset, 1);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        return messageService.get(request);
    }

    @Override
    public MessageResult getMessage(MessageRequest request) {
        return messageService.getMessage(request);
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
        DequeueRequest request = DequeueRequest.builder()
            .topic(topic)
            .queueId(queueId)
            .offset(offset)
            .maxNum(num)
            .build();

        DequeueResult result = get(request);
        return result.getMessageList();
    }

}
