package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueueGateway;
import java.util.List;
import lombok.NonNull;

public class MessageService {
    private final CommitLog commitLog;
    private final ConsumeQueueGateway consumeQueueGateway;

    public MessageService(
        CommitLog commitLog,
        ConsumeQueueGateway consumeQueueGateway
    ) {
        this.commitLog = commitLog;
        this.consumeQueueGateway = consumeQueueGateway;
    }

    public MessageResult getMessage(MessageRequest request) {
        MessageBO message = commitLog.select(request.getOffset());

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
        List<QueueUnit> unitList = consumeQueueGateway.get(
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
            messageBO = commitLog.select(unit.getCommitOffset(), unit.getMessageSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
        }

        result.setStatusByMessageList();
        return result;
    }

}
