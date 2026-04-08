package cn.coderule.wolfmq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import cn.coderule.wolfmq.broker.infra.store.SubscriptionStore;
import cn.coderule.wolfmq.broker.server.bootstrap.BrokerContext;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;

public class RenewBootstrap implements Lifecycle {
    private BrokerConfig brokerConfig;

    private RenewListener renewListener;
    private DefaultReceiptHandler receiptHandler;
    private RenewService renewService;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

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
        InvisibleService invisibleService = BrokerContext.getBean(InvisibleService.class);
        renewListener = new RenewListener(invisibleService);
    }

    private void initReceiptHandler() {
        receiptHandler = new DefaultReceiptHandler(brokerConfig, renewListener);
        BrokerContext.register(receiptHandler, ReceiptHandler.class);
    }

    private void initRenewService() throws Exception {
        renewService = new RenewService(
            brokerConfig,
            receiptHandler,
            renewListener,
            BrokerContext.getBean(ConsumerManager.class),
            BrokerContext.getBean(SubscriptionStore.class)
        );

        renewService.initialize();
    }

    private void initReceiptListener() {
        ReceiptListener receiptListener = new ReceiptListener(receiptHandler);

        ConsumerManager register = BrokerContext.getBean(ConsumerManager.class);
        register.addListener(receiptListener);
    }

}
