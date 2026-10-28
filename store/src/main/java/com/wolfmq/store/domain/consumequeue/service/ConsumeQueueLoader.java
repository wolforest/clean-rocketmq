package com.wolfmq.store.domain.consumequeue.service;

import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.consumequeue.ConsumeQueue;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueRegistry;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueLoader implements ConsumeQueueRegistry {
    private final ConsumeQueueConfig config;

    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);

    public ConsumeQueueLoader(ConsumeQueueConfig config) {
        this.config = config;
    }
    @Override
    public void register(ConsumeQueue queue) {
        queueSet.add(queue);
    }

    public void load() {
        if (queueSet.isEmpty()) return;

        queueSet.forEach(queue -> {
            MappedFileQueue mappedFileQueue = queue.getMappedFileQueue();
            mappedFileQueue.load();
            mappedFileQueue.checkSelf();
        });
    }
}
