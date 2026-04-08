package cn.coderule.wolfmq.store.domain.mq.queue;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import java.util.List;
import lombok.NonNull;

public class MessageService {
    private final CommitLogManager commitLogManager;
    private final ConsumeQueueManager consumeQueueManager;

    public MessageService(
        CommitLogManager commitLogManager,
        ConsumeQueueManager consumeQueueManager
    ) {
        this.commitLogManager = commitLogManager;
        this.consumeQueueManager = consumeQueueManager;
    }

    public MessageResult getMessage(MessageRequest request) {
        MessageBO message = commitLogManager.select(request.getOffset());

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
            messageBO = commitLogManager.select(unit.getCommitOffset(), unit.getMessageSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
        }

        result.setStatusByMessageList();
        return result;
    }

}
