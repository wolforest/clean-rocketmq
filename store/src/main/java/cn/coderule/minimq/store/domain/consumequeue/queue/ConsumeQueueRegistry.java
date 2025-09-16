package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
