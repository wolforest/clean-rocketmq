package cn.coderule.minimq.store.domain.consumequeue.service;

import cn.coderule.minimq.domain.config.ConsumeQueueConfig;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueue;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueRegistry;
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
