package com.wolf.minimq.store.domain.consumequeue.queue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
