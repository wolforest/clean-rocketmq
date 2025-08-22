package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.infra.store.TopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;

public class PopManager implements Lifecycle {
    private BrokerConfig brokerConfig;

    private PopContextBuilder contextBuilder;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(brokerConfig.getCluster());

        initContextBuilder();


    }

    private void initContextBuilder() {
        contextBuilder = new PopContextBuilder(brokerConfig,
                BrokerContext.getBean(ConsumerRegister.class),
                BrokerContext.getBean(TopicStore.class),
                BrokerContext.getBean(SubscriptionStore.class));

        BrokerContext.register(contextBuilder);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
