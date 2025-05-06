package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.domain.enums.store.QueueType;
import cn.coderule.minimq.domain.domain.model.store.CommitLogEvent;
import cn.coderule.minimq.domain.domain.model.store.QueueUnit;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueue;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorConsumeQueue implements ConsumeQueue {
    public static final ErrorConsumeQueue INSTANCE = new ErrorConsumeQueue();

    public static ErrorConsumeQueue singleton() {
        log.error("no such queue store");
        return INSTANCE;
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.ERROR;
    }

    @Override
    public String getTopic() {
        return "NO_SUCH_TOPIC";
    }

    @Override
    public int getQueueId() {
        return 0;
    }

    @Override
    public int getUnitSize() {
        return 0;
    }

    @Override
    public void enqueue(CommitLogEvent event) {

    }

    @Override
    public QueueUnit get(long index) {
        return null;
    }

    @Override
    public List<QueueUnit> get(long index, int num) {
        return List.of();
    }

    @Override
    public long getMinOffset() {
        return 0;
    }

    @Override
    public void setMinOffset(long offset) {

    }

    @Override
    public long getMaxOffset() {
        return 0;
    }

    @Override
    public void setMaxOffset(long maxOffset) {

    }

    @Override
    public long getMaxCommitLogOffset() {
        return 0;
    }

    @Override
    public void setMaxCommitLogOffset(long maxCommitLogOffset) {

    }

    @Override
    public long increaseOffset() {
        return 0;
    }

    @Override
    public MappedFileQueue getMappedFileQueue() {
        return null;
    }
}
