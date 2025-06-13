package cn.coderule.minimq.broker.domain.consumer.consumer;

import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerRegister {
    public boolean register(ConsumerInfo consumerInfo) {
        return true;
    }

    public boolean unregister(ConsumerInfo consumerInfo) {
        return true;
    }
}
