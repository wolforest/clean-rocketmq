package com.wolfmq.store.domain.consumequeue.queue;

import com.wolfmq.common.domain.consumequeue.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
