package com.wolf.minimq.store.domain.mq;

import com.wolf.common.util.collection.CollectionUtil;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.model.dto.InsertFuture;
import com.wolf.minimq.domain.model.dto.GetRequest;
import com.wolf.minimq.domain.model.dto.GetResult;
import com.wolf.minimq.domain.utils.lock.ConsumeQueueLock;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.service.store.domain.MessageStore;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.server.StoreContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageStore implements MessageStore {
    private final ConsumeQueueLock consumeQueueLock;
    private final MessageConfig messageConfig;
    private final ConsumeQueueStore consumeQueueStore;
    private final CommitLog commitLog;

    public DefaultMessageStore(
        MessageConfig messageConfig,
        CommitLog commitLog,
        ConsumeQueueStore consumeQueueStore) {

        this.messageConfig = messageConfig;
        this.commitLog = commitLog;
        this.consumeQueueStore = consumeQueueStore;

        this.consumeQueueLock = new ConsumeQueueLock();
    }

    /**
     * enqueue single/multi message
     *  - assign consumeQueue offset
     *  - append commitLog
     *  - increase consumeQueue offset
     *
     * @param messageBO messageContext
     * @return EnqueueResult
     */
    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return waitForResult(enqueueAsync(messageBO));
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        consumeQueueLock.lock(messageBO.getTopic(), messageBO.getQueueId());
        try {
            long queueOffset = consumeQueueStore.assignOffset(messageBO.getTopic(), messageBO.getQueueId());
            messageBO.setQueueOffset(queueOffset);

            InsertFuture result = commitLog.insert(messageBO);

            if (result.isInsertSuccess()) {
                consumeQueueStore.increaseOffset(messageBO.getTopic(), messageBO.getQueueId());
            }

            return result.getFuture();
        } catch (Exception e) {
            return CompletableFuture.completedFuture(EnqueueResult.failure());
        } finally {
            consumeQueueLock.unlock(messageBO.getTopic(), messageBO.getQueueId());
        }
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        return get(topic, queueId, offset, 1);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        GetRequest request = GetRequest.builder()
            .topic(topic)
            .queueId(queueId)
            .offset(offset)
            .num(num)
            .maxSize(messageConfig.getMaxSize())
            .build();
        return get(request);
    }

    @Override
    public GetResult get(GetRequest request) {
        List<QueueUnit> unitList = consumeQueueStore.get(
            request.getTopic(), request.getQueueId(), request.getOffset(), request.getNum()
        );

        if (CollectionUtil.isEmpty(unitList)) {
            return GetResult.notFound();
        }

        return getByUnitList(unitList);
    }

    private GetResult getByUnitList(@NonNull List<QueueUnit> unitList) {
        GetResult result = new GetResult();
        MessageBO messageBO;
        for (QueueUnit unit : unitList) {
            messageBO = commitLog.select(unit.getCommitLogOffset(), unit.getUnitSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
        }

        return result;
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        List<MessageBO> messageList = getMessage(topic, queueId, offset, 1);

        return CollectionUtil.isEmpty(messageList)
            ? null
            : messageList.getFirst();
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        GetResult result = get(topic, queueId, offset, num);
        return result.getMessageList();
    }

    private EnqueueResult waitForResult(CompletableFuture<EnqueueResult> future) {
        try {
            StoreConfig config = StoreContext.getBean(StoreConfig.class);
            int timeout = config.getSyncFlushTimeout() + 5 * 1000;
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("enqueue error:", e);
            return EnqueueResult.failure();
        }
    }
}
