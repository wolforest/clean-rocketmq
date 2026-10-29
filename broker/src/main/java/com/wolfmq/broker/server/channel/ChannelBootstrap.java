package com.wolfmq.broker.server.channel;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.api.ConsumerController;
import com.wolfmq.broker.api.ProducerController;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.core.constant.ContextNamespace;

public class ChannelBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private ChannelScheduler channelScheduler;

    public ChannelBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
        channelScheduler = new ChannelScheduler(brokerConfig);
        channelScheduler.initialize();
    }

    @Override
    public void start() throws Exception {
        channelScheduler.inject(
            context.getBean(ProducerController.class, ContextNamespace.APP),
            context.getBean(ConsumerController.class, ContextNamespace.APP)
        );
        channelScheduler.start();
    }

    @Override
    public void shutdown() throws Exception {
        channelScheduler.shutdown();
    }

}
