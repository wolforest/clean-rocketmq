package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.rpc.broker.protocol.consumer.ConsumerInfo;

public class Consumer  {
    public boolean register(ConsumerInfo consumerInfo) {
        return true;
    }

    public boolean unregister(ConsumerInfo consumerInfo) {
        return true;
    }
}
