package com.wolfmq.store.domain.messagequeue;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import com.wolfmq.common.domain.messagequeue.dto.DequeueRequest;
import com.wolfmq.common.domain.messagequeue.dto.DequeueResult;
import com.wolfmq.common.domain.consumer.consume.dto.MessageRequest;
import com.wolfmq.common.domain.consumer.consume.dto.MessageResult;
import com.wolfmq.common.domain.messagequeue.MQService;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueResult;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.store.domain.messagequeue.queue.DequeueService;
import com.wolfmq.store.domain.messagequeue.queue.EnqueueService;
import com.wolfmq.store.domain.messagequeue.queue.MessageService;
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
            .topicName(topic)
            .queueId(queueId)
            .offset(offset)
            .num(num)
            .build();

        DequeueResult result = get(request);
        return result.getMessageList();
    }

}
