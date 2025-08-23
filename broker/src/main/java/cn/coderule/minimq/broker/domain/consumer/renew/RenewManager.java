package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class RenewManager implements Lifecycle {
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
    }

    private void initRenewService() throws Exception {
        renewService = new RenewService(
            brokerConfig,
            receiptHandler,
            renewListener,
            BrokerContext.getBean(ConsumerRegister.class),
            BrokerContext.getBean(SubscriptionStore.class)
        );

        renewService.initialize();
    }

    private void initReceiptListener() {
        ReceiptListener receiptListener = new ReceiptListener(receiptHandler);

        ConsumerRegister register = BrokerContext.getBean(ConsumerRegister.class);
        register.addListener(receiptListener);
    }

}
