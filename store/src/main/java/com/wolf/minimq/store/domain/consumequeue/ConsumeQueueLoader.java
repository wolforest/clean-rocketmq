package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
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
