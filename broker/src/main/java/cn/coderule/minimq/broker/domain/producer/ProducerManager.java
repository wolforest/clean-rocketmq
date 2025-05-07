package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.api.validator.MessageValidator;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;

public class ProducerManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private MessageSender messageSender;
    private Producer producer;

    @Override
    public void initialize() {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        this.messageSender = new MessageSender(brokerConfig);
        this.producer = new Producer(messageSender);

        MessageConfig messageConfig = BrokerContext.getBean(MessageConfig.class);
        ProducerController controller = new ProducerController(messageConfig, producer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() {
        messageSender.start();
    }

    @Override
    public void shutdown() {
        messageSender.shutdown();
    }


}
