package com.wolfmq.registry.domain.store.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import com.wolfmq.common.config.server.RegistryConfig;
import com.wolfmq.registry.domain.store.StoreRegistry;
import com.wolfmq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnregisterService extends ServiceThread {
    private final StoreRegistry registry;

    private final BlockingQueue<UnRegisterBrokerRequestHeader> queue;

    public UnregisterService(RegistryConfig config, StoreRegistry registry) {
        this.registry = registry;
        queue = new LinkedBlockingQueue<>(config.getUnregisterQueueCapacity());
    }

    @Override
    public String getServiceName() {
        return UnregisterService.class.getSimpleName();
    }

    public boolean submit(UnRegisterBrokerRequestHeader request) {
        return queue.offer(request);
    }

    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            schedule();
        }
    }

    private static final long POLL_TIMEOUT_MS = 100;

    private void schedule() {
        try {
            // poll with timeout instead of take(): blocking take() ignores the
            // ServiceThread stop flag and makes shutdown hang for JOIN_TIME
            UnRegisterBrokerRequestHeader request = queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (null == request) {
                return;
            }

            Set<UnRegisterBrokerRequestHeader> requests = new HashSet<>();
            requests.add(request);

            queue.drainTo(requests);

            registry.unregister(requests);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            log.error("unregister store error", t);
        }
    }
}
