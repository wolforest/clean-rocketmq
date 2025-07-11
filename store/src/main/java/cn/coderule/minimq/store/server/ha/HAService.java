package cn.coderule.minimq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.monitor.StateMonitor;
import cn.coderule.minimq.store.server.ha.core.monitor.StateRequest;

public class HAService implements Lifecycle {
    private final StoreConfig storeConfig;
    private final HAServer haServer;
    private final HAClient haClient;
    private final StateMonitor stateMonitor;

    public HAService(StoreConfig storeConfig, HAServer haServer) {
        this(storeConfig, haServer, null);
    }

    public HAService(StoreConfig storeConfig, HAServer haServer, HAClient haClient) {
        this.storeConfig = storeConfig;
        this.haServer = haServer;
        this.haClient = haClient;
        this.stateMonitor = new StateMonitor(storeConfig, haServer, haClient);
    }

    public void monitorState(StateRequest request) {
        this.stateMonitor.setRequest(request);
    }

    @Override
    public void start() {
        this.stateMonitor.start();
    }

    @Override
    public void shutdown() {
        this.stateMonitor.stop();
    }
}
