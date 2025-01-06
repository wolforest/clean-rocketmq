package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.common.lang.concurrent.ServiceThread;
import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.store.server.StoreCheckpoint;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueFlusher extends ServiceThread {
    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);

    private final StoreCheckpoint checkpoint;
    private final ConsumeQueueConfig config;

    public ConsumeQueueFlusher(StoreCheckpoint checkpoint, ConsumeQueueConfig config) {
        this.checkpoint = checkpoint;
        this.config = config;
    }

    public void register(ConsumeQueue queue) {
        queueSet.add(queue);
    }

    @Override
    public String getServiceName() {
        return ConsumeQueueFlusher.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", getServiceName());

        while (!this.isStopped()) {
            try {
                int interval = config.getFlushInterval();
                await(interval);
                flush();
            } catch (Exception e) {
                log.warn("{} occurs exception.", getServiceName(), e);
            }
        }

        log.info("{} service end", getServiceName());
    }

    private void flush() {
        if (queueSet.isEmpty()) return;

        int flushPages = config.getMinFlushPages();
        queueSet.forEach(queue -> {
            MappedFileQueue mappedFileQueue = queue.getMappedFileQueue();
            mappedFileQueue.flush(flushPages);
        });
    }


}
