package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.lang.concurrent.ServiceThread;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnregisterService extends ServiceThread {
    private final RegistryConfig config;
    private final StoreRegistry registry;

    private final BlockingQueue<UnRegisterBrokerRequestHeader> queue;

    public UnregisterService(RegistryConfig config, StoreRegistry registry) {
        this.config = config;
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

    private void schedule() {
        try {
            Set<UnRegisterBrokerRequestHeader> requests = new HashSet<>();

            // from the opensource code
            // UnRegisterBrokerRequestHeader request = queue.take();
            // requests.add(request);
            queue.drainTo(requests);

            registry.unregister(requests);
        } catch (Throwable t) {
            log.error("unregister store error", t);
        }
    }
}
