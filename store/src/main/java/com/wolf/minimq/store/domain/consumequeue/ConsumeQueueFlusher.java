package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.common.lang.concurrent.ServiceThread;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConsumeQueueFlusher extends ServiceThread {
    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);
    public void register(ConsumeQueue queue) {
        queueSet.add(queue);
    }

    @Override
    public String getServiceName() {
        return ConsumeQueueFlusher.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    public void flush(int minPages) {
        if (queueSet.isEmpty()) return;

        queueSet.forEach(queue -> {
            MappedFileQueue mappedFileQueue = queue.getMappedFileQueue();
            mappedFileQueue.flush(minPages);
        });
    }
}
