package cn.coderule.minimq.broker.infra.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.infra.embed.EmbedMQStore;
import cn.coderule.minimq.broker.infra.embed.EmbedStoreManager;
import cn.coderule.minimq.broker.infra.remote.RemoteMQStore;
import cn.coderule.minimq.broker.infra.remote.RemoteStoreManager;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class StoreManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private final EmbedStoreManager embedManager;
    private final RemoteStoreManager remoteManager;

    public StoreManager() {
        this.embedManager = new EmbedStoreManager();
        this.remoteManager = new RemoteStoreManager();
    }

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        embedManager.initialize();
        remoteManager.initialize();
    }

    @Override
    public void start() throws Exception {
        embedManager.start();
        remoteManager.start();

    }

    @Override
    public void shutdown() throws Exception {
        embedManager.shutdown();
        remoteManager.shutdown();
    }

    private void initServices() {

    }

    private void initMQStore() {
        EmbedMQStore embedMQStore = BrokerContext.getBean(EmbedMQStore.class);
        RemoteMQStore remoteMQStore = BrokerContext.getBean(RemoteMQStore.class);
        MQStore mqStore = new MQStore(brokerConfig, embedMQStore, remoteMQStore);
        BrokerContext.register(mqStore);
    }
}
