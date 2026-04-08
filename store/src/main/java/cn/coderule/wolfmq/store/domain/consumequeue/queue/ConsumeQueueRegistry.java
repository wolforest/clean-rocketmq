package cn.coderule.wolfmq.store.domain.consumequeue.queue;

import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
