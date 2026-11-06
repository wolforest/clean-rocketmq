package com.wolfmq.store.domain.messagequeue.queue;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import com.wolfmq.common.domain.messagequeue.dto.DequeueResult;
import com.wolfmq.common.domain.messagequeue.dto.DequeueRequest;
import com.wolfmq.common.domain.consumequeue.QueueUnit;
import com.wolfmq.common.domain.consumer.consume.dto.MessageRequest;
import com.wolfmq.common.domain.consumer.consume.dto.MessageResult;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import java.util.List;
import lombok.NonNull;

public class MessageService {
    private final CommitLogFacade commitLogFacade;
    private final ConsumeQueueFacade consumeQueueFacade;

    public MessageService(
        CommitLogFacade commitLogFacade,
        ConsumeQueueFacade consumeQueueFacade
    ) {
        this.commitLogFacade = commitLogFacade;
        this.consumeQueueFacade = consumeQueueFacade;
    }

    public MessageResult getMessage(MessageRequest request) {
        MessageBO message = commitLogFacade.select(request.getOffset());

        if (!message.isValid()) {
            return MessageResult.notFound();
        }

        return MessageResult.success(message);
    }

    public DequeueResult get(String topic, int queueId, long offset, int num) {
        DequeueRequest request = DequeueRequest.builder()
            .topicName(topic)
            .queueId(queueId)
            .offset(offset)
            .num(num)
            .build();
        return get(request);
    }

    public DequeueResult get(DequeueRequest request) {
        List<QueueUnit> unitList = consumeQueueFacade.get(
            request.getTopicName(), request.getQueueId(), request.getOffset(), request.getNum()
        );

        if (CollectionUtil.isEmpty(unitList)) {
            return DequeueResult.notFound();
        }

        return getByUnitList(unitList);
    }

    private DequeueResult getByUnitList(@NonNull List<QueueUnit> unitList) {
        DequeueResult result = new DequeueResult();
        MessageBO messageBO;

        for (QueueUnit unit : unitList) {
            messageBO = commitLogFacade.select(unit.getCommitOffset(), unit.getMessageSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
        }

        result.setStatusByMessageList();
        return result;
    }

}
