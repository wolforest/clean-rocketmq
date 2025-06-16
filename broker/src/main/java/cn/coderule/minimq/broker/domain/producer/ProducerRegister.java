package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.domain.config.BrokerConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProducerRegister {
    private final BrokerConfig brokerConfig;

    public ProducerRegister(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }
}
