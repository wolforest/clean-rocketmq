package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConsumeQueueRecovery implements ConsumeQueueRegistry {
    private final ConsumeQueueConfig config;

    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);

    public ConsumeQueueRecovery(ConsumeQueueConfig config) {
        this.config = config;
    }
    @Override
    public void register(ConsumeQueue queue) {
        queueSet.add(queue);
    }

    public void recover() {
        if (queueSet.isEmpty()) return;

        queueSet.forEach(queue -> {
            recoverQueue(queue);
        });
    }

    private void recoverQueue(ConsumeQueue queue) {
    }
}
