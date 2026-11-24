package com.wolfmq.store.domain.consumequeue.service;

import com.wolfmq.store.domain.consumequeue.file.FileConsumeQueue;

public interface ConsumeQueueRegistry {
    void register(FileConsumeQueue queue);
}
