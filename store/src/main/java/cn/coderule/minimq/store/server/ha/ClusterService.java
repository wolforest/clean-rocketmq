package cn.coderule.minimq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.store.infra.StoreRegister;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * cluster service:
 * - communicate with registry
 *   - register master
 *   - register slave if no master registered
 *   - registry heartbeat
 * - initialize M/S
 *   - send ha info
 *   - handshake
 *   - syn metadata
 */
public class ClusterService implements Lifecycle {
    private final StoreConfig storeConfig;
    private final StoreRegister storeRegister;
    private final ScheduledExecutorService heartbeatScheduler;

    private boolean registered = false;

    public ClusterService(StoreConfig storeConfig, StoreRegister storeRegister) {
        this.storeConfig = storeConfig;
        this.storeRegister = storeRegister;

        heartbeatScheduler = ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("StoreHeartbeatThread_")
        );
    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
        registerStore();
    }

    @Override
    public void shutdown() {
        heartbeatScheduler.shutdown();
        unregisterStore();
    }

    private void registerStore() {
        if (!registered) {
            return;
        }

        storeRegister.registerStore();
    }

    private void unregisterStore() {
        if (!registered) {
            return;
        }

        storeRegister.unregisterStore();
    }

    private void startHeartbeat() {
        if (!registered) {
            return;
        }

        heartbeatScheduler.scheduleAtFixedRate(
            storeRegister::heartbeat,
            1000,
            storeConfig.getRegistryHeartbeatInterval(),
            TimeUnit.MILLISECONDS
        );
    }


}
