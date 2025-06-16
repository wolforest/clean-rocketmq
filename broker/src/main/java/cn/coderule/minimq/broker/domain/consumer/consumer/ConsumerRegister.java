package cn.coderule.minimq.broker.domain.consumer.consumer;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.dto.running.ConsumerGroupInfo;
import cn.coderule.minimq.domain.service.broker.listener.ConsumerListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerRegister {
    private final BrokerConfig brokerConfig;

    private final long channelExpireTime;
    private final long subscriptionExpireTime;

    private final ConcurrentMap<String, ConsumerGroupInfo> groupMap;
    private final ConcurrentMap<String, Consumer> compensationMap;
    private final List<ConsumerListener> listeners;

    public ConsumerRegister(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.channelExpireTime = brokerConfig.getChannelExpireTime();
        this.subscriptionExpireTime = brokerConfig.getSubscriptionExpireTime();

        this.groupMap = new ConcurrentHashMap<>(1024);
        this.compensationMap = new ConcurrentHashMap<>(1024);
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public boolean register(ConsumerInfo consumerInfo) {
        return true;
    }

    public boolean unregister(ConsumerInfo consumerInfo) {
        return true;
    }
}
