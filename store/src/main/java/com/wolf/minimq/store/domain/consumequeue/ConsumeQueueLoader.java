package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConsumeQueueLoader {
    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);
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
