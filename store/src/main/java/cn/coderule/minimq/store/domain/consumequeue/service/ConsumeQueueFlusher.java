package cn.coderule.minimq.store.domain.consumequeue.service;

import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueRegistry;
import cn.coderule.common.lang.concurrent.ServiceThread;
import cn.coderule.minimq.domain.config.ConsumeQueueConfig;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueue;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueFlusher extends ServiceThread implements ConsumeQueueRegistry {
    private final ConsumeQueueConfig config;
    private final StoreCheckpoint checkpoint;

    private long lastFlushTime = 0;
    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);

    public ConsumeQueueFlusher(ConsumeQueueConfig config, StoreCheckpoint checkpoint) {
        this.config = config;
        this.checkpoint = checkpoint;
    }

    @Override
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

        long now = System.currentTimeMillis();
        int minFlushPages = config.getMinFlushPages();

        if (now - lastFlushTime >= config.getFlushInterval()) {
            minFlushPages = 0;
        }

        for (ConsumeQueue queue : queueSet) {
            MappedFileQueue mappedFileQueue = queue.getMappedFileQueue();
            mappedFileQueue.flush(minFlushPages);
        }

        lastFlushTime = now;
    }


}
