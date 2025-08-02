package cn.coderule.minimq.broker.infra.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.infra.embed.EmbedStoreManager;
import cn.coderule.minimq.broker.infra.remote.RemoteStoreManager;

public class StoreManager implements Lifecycle {
    private final EmbedStoreManager embedStoreManager;
    private final RemoteStoreManager remoteStoreManager;

    public StoreManager(EmbedStoreManager embedStoreManager, RemoteStoreManager remoteStoreManager) {
        this.embedStoreManager = embedStoreManager;
        this.remoteStoreManager = remoteStoreManager;
    }

    @Override
    public void initialize() throws Exception {
        embedStoreManager.initialize();
        remoteStoreManager.initialize();
    }

    @Override
    public void start() throws Exception {
        embedStoreManager.start();
        remoteStoreManager.start();

    }

    @Override
    public void shutdown() throws Exception {
        embedStoreManager.shutdown();
        remoteStoreManager.shutdown();
    }
}
