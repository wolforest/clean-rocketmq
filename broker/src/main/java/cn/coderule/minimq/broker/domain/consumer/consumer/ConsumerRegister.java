package cn.coderule.minimq.broker.domain.consumer.consumer;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.dto.running.ConsumerGroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.service.broker.listener.ConsumerListener;
import io.netty.channel.Channel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private final ConcurrentMap<String, ConsumerGroupInfo> compensationMap;
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

    public void scanIdleChannels() {
        Iterator<Map.Entry<String, ConsumerGroupInfo>> iterator = groupMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ConsumerGroupInfo> entry = iterator.next();

            ConcurrentMap<Channel, ClientChannelInfo> channelInfoTable = entry.getValue().getChannelInfoTable();
            scanIdleInChannelMap(channelInfoTable);

            if (channelInfoTable.isEmpty()) {
                log.warn("SCAN: remove expired channel from ConsumerRegister: group={}", entry.getKey());
                iterator.remove();
            }
        }

        this.removeIdleChannels();
    }

    private void scanIdleInChannelMap(ConcurrentMap<Channel, ClientChannelInfo> channelInfoTable) {
        Iterator<Map.Entry<Channel, ClientChannelInfo>> iterator = channelInfoTable.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Channel, ClientChannelInfo> entry = iterator.next();
            ClientChannelInfo channelInfo = entry.getValue();

            long now = System.currentTimeMillis();
            if (now - channelInfo.getLastUpdateTimestamp() <= channelExpireTime) {
                continue;
            }

            closeChannel(channelInfo);
            iterator.remove();
        }
    }

    private void closeChannel(ClientChannelInfo channelInfo) {

    }

    private void removeIdleChannels() {

    }
}
