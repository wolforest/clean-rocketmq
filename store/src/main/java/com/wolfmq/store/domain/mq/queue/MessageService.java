package com.wolfmq.store.domain.mq.queue;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.consumequeue.QueueUnit;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import java.util.List;
import lombok.NonNull;

public class MessageService {
    private final CommitLogProxy commitLogProxy;
    private final ConsumeQueueManager consumeQueueManager;

    public MessageService(
        CommitLogProxy commitLogProxy,
        ConsumeQueueManager consumeQueueManager
    ) {
        this.commitLogProxy = commitLogProxy;
        this.consumeQueueManager = consumeQueueManager;
    }

    public MessageResult getMessage(MessageRequest request) {
        MessageBO message = commitLogProxy.select(request.getOffset());

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
        List<QueueUnit> unitList = consumeQueueManager.get(
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
            messageBO = commitLogProxy.select(unit.getCommitOffset(), unit.getMessageSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
        }

        result.setStatusByMessageList();
        return result;
    }

}
