package cn.coderule.minimq.broker.domain.consumer.consumer;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.dto.running.ConsumerGroupInfo;
import cn.coderule.minimq.domain.domain.enums.consume.ConsumerEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.service.broker.listener.ConsumerListener;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyHelper;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            scanIdleInChannelMap(channelInfoTable, entry.getKey(), entry.getValue().getSubscribeTopics());

            if (channelInfoTable.isEmpty()) {
                log.warn("SCAN: remove expired channel from ConsumerRegister: group={}", entry.getKey());
                iterator.remove();
            }
        }

        this.removeIdleChannels();
    }

    private void scanIdleInChannelMap(ConcurrentMap<Channel, ClientChannelInfo> channelInfoTable, String consumerGroup, Set<String> topics) {
        Iterator<Map.Entry<Channel, ClientChannelInfo>> iterator = channelInfoTable.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Channel, ClientChannelInfo> entry = iterator.next();
            ClientChannelInfo channelInfo = entry.getValue();

            long now = System.currentTimeMillis();
            if (now - channelInfo.getLastUpdateTimestamp() <= channelExpireTime) {
                continue;
            }

            closeChannel(channelInfo, consumerGroup, topics);
            iterator.remove();
        }
    }

    private void closeChannel(ClientChannelInfo channelInfo, String consumerGroup, Set<String> topics) {
        log.warn("SCAN: close idle channel from ConsumerRegister, channel={}, consumerGroup={}",
            NettyHelper.getRemoteAddr(channelInfo.getChannel()),
            consumerGroup
        );

        invokeListeners(ConsumerEvent.CLIENT_UNREGISTER, consumerGroup, channelInfo, topics);
        NettyHelper.close(channelInfo.getChannel());
    }

    private void invokeListeners(ConsumerEvent event, String group, Object... args) {
        if (listeners.isEmpty()) {
            return;
        }

        for (ConsumerListener listener : listeners) {
            try {
                listener.handle(event, group, args);
            } catch (Throwable t) {
                log.error("invoke listener error", t);
            }
        }
    }

    private List<String> getIdleTopicList(ConcurrentMap<String, SubscriptionData> subscriptionTable) {
        List<String> topicList = new ArrayList<>();
        subscriptionTable.forEach((topic, subscriptionData) -> {
            long now = System.currentTimeMillis();
            if (now - subscriptionData.getSubVersion() > subscriptionExpireTime) {
                topicList.add(topic);
            }
        });

        return topicList;
     }

    private List<String> getIdleGroupList() {
        List<String> removeList = new ArrayList<>();
        compensationMap.forEach((group, info) -> {
            ConcurrentMap<String, SubscriptionData> subscriptionTable = info.getSubscriptionTable();
            List<String> topicList = getIdleTopicList(subscriptionTable);

            for (String topic : topicList) {
                subscriptionTable.remove(topic);
                if (subscriptionTable.isEmpty()) {
                    removeList.add(group);
                }
            }
        });

        return removeList;
    }

    private void removeIdleChannels() {
        List<String> removeList = getIdleGroupList();

        for (String group : removeList) {
            compensationMap.remove(group);
        }
    }
}
