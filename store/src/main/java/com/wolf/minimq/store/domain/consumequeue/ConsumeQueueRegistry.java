package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(ConsumeQueue queue);
}
