package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.cluster.store.QueueUnit;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import java.util.List;
import lombok.NonNull;

public class MessageService {
    private final MessageConfig messageConfig;
    private final CommitLog commitLog;
    private final ConsumeQueueGateway consumeQueueGateway;

    public MessageService(
        StoreConfig storeConfig,
        CommitLog commitLog,
        ConsumeQueueGateway consumeQueueGateway
    ) {
        this.messageConfig = storeConfig.getMessageConfig();

        this.commitLog = commitLog;
        this.consumeQueueGateway = consumeQueueGateway;
    }

    public MessageResult getMessage(MessageRequest request) {
        MessageBO message = commitLog.select(request.getOffset(), request.getSize());
        if (message == null) {
            return MessageResult.notFound();
        }

        return MessageResult.success(message);
    }

    public DequeueResult get(String topic, int queueId, long offset, int num) {
        DequeueRequest request = DequeueRequest.builder()
            .topic(topic)
            .queueId(queueId)
            .offset(offset)
            .num(num)
            .maxNum(messageConfig.getMaxRequestSize())
            .build();
        return get(request);
    }

    public DequeueResult get(DequeueRequest request) {
        List<QueueUnit> unitList = consumeQueueGateway.get(
            request.getTopic(), request.getQueueId(), request.getOffset(), request.getNum()
        );

        if (CollectionUtil.isEmpty(unitList)) {
            return DequeueResult.notFound();
        }

        return getByUnitList(unitList);
    }

    private DequeueResult getByUnitList(@NonNull List<QueueUnit> unitList) {
        DequeueResult result = new DequeueResult();
        MessageBO messageBO;
        long maxOffset = 0;

        for (QueueUnit unit : unitList) {
            messageBO = commitLog.select(unit.getCommitLogOffset(), unit.getUnitSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
            if (messageBO.getQueueOffset() > maxOffset) {
                maxOffset = messageBO.getQueueOffset();
            }
        }

        result.setNextOffset(maxOffset + 1);
        return result;
    }


}
