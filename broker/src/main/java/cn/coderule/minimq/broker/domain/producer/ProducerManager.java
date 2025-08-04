package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.business.MessageConfig;

public class ProducerManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private MessageSender messageSender;
    private Producer producer;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        MQStore messageStore = BrokerContext.getBean(MQStore.class);
        this.messageSender = new MessageSender(brokerConfig, messageStore);
        this.producer = new Producer(messageSender);

        MessageConfig messageConfig = brokerConfig.getMessageConfig();
        ProducerController controller = new ProducerController(brokerConfig, messageConfig, producer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() throws Exception {
        messageSender.start();
    }

    @Override
    public void shutdown() throws Exception {
        messageSender.shutdown();
    }


}
