package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;

public class RenewManager implements Lifecycle {
    private BrokerConfig brokerConfig;

    private RenewListener renewListener;
    private ReceiptHandler receiptHandler;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        initRenewListener();
        initReceiptHandler();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    private void initRenewListener() {
        InvisibleService invisibleService = BrokerContext.getBean(InvisibleService.class);
        renewListener = new RenewListener(invisibleService);
    }

    private void initReceiptHandler() {
        receiptHandler = new DefaultReceiptHandler(brokerConfig, renewListener);
    }




}
