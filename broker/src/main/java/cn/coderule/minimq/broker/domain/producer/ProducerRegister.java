package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.lang.concurrent.atomic.PositiveAtomicCounter;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.service.broker.listener.ProducerListener;
import io.netty.channel.Channel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProducerRegister {
    private final BrokerConfig brokerConfig;

    private final long channelExpireTime;
    private final int maxChannelFetchTimes;

    private final PositiveAtomicCounter counter;
    private final List<ProducerListener> listenerList;
    // groupName -> channel
    private final ConcurrentMap<String, Channel> channelMap;
    // groupName -> channel -> channelInfo
    private final ConcurrentMap<String, ConcurrentMap<Channel, ClientChannelInfo>> channelTree;

    public ProducerRegister(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.channelExpireTime = brokerConfig.getChannelExpireTime();
        this.maxChannelFetchTimes = brokerConfig.getMaxChannelFetchTimes();

        this.counter = new PositiveAtomicCounter();
        this.channelTree = new ConcurrentHashMap<>();
        this.channelMap = new ConcurrentHashMap<>();
        this.listenerList = new CopyOnWriteArrayList<>();
    }
}
