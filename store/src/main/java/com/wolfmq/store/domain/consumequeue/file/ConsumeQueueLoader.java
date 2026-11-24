package com.wolfmq.store.domain.consumequeue.file;

import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueRegistry;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueLoader implements ConsumeQueueRegistry {
    private final ConsumeQueueConfig config;

    private final Set<FileConsumeQueue> queueSet = new LinkedHashSet<>(128);

    public ConsumeQueueLoader(ConsumeQueueConfig config) {
        this.config = config;
    }
    @Override
    public void register(FileConsumeQueue queue) {
        queueSet.add(queue);
    }

    public void load() {
        if (queueSet.isEmpty()) return;

        queueSet.forEach(queue -> {
            queue.getMappedFileQueue().load();
            queue.getMappedFileQueue().checkSelf();
        });
    }
}
