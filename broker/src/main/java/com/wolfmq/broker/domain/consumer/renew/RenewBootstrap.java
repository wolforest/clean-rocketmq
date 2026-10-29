package com.wolfmq.broker.domain.consumer.renew;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.consumer.ack.InvisibleService;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;

public class RenewBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public RenewBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;

    private RenewListener renewListener;
    private DefaultReceiptHandler receiptHandler;
    private RenewService renewService;

    @Override
    public void initialize() throws Exception {
        brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);

        initRenewListener();
        initReceiptHandler();
        initRenewService();
        initReceiptListener();
    }

    @Override
    public void start() throws Exception {
        renewService.start();
        receiptHandler.start();
    }

    @Override
    public void shutdown() throws Exception {
        renewService.shutdown();
        receiptHandler.shutdown();
    }

    private void initRenewListener() {
        InvisibleService invisibleService = context.getBean(InvisibleService.class, ContextNamespace.APP);
        renewListener = new RenewListener(invisibleService);
    }

    private void initReceiptHandler() {
        receiptHandler = new DefaultReceiptHandler(brokerConfig, renewListener);
        context.register(receiptHandler, ReceiptHandler.class, ContextNamespace.APP);
    }

    private void initRenewService() throws Exception {
        renewService = new RenewService(
            brokerConfig,
            receiptHandler,
            renewListener,
            context.getBean(ConsumerManager.class, ContextNamespace.APP),
            context.getBean(SubscriptionStore.class, ContextNamespace.APP)
        );

        renewService.initialize();
    }

    private void initReceiptListener() {
        ReceiptListener receiptListener = new ReceiptListener(receiptHandler);

        ConsumerManager register = context.getBean(ConsumerManager.class, ContextNamespace.APP);
        register.addListener(receiptListener);
    }

}
