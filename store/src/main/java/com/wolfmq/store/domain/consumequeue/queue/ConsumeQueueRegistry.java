package com.wolfmq.store.domain.consumequeue.queue;

import com.wolfmq.domain.domain.consumequeue.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
