package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.lang.concurrent.atomic.PositiveAtomicCounter;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.enums.produce.ProducerEvent;
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
    // clientId -> channel
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

    public void register(String groupName, ClientChannelInfo channelInfo) {
        ConcurrentMap<Channel, ClientChannelInfo> map = channelTree.computeIfAbsent(
            groupName, k -> new ConcurrentHashMap<>()
        );

        ClientChannelInfo oldInfo = map.get(channelInfo.getChannel());
        if (oldInfo != null) {
            oldInfo.setLastUpdateTime(System.currentTimeMillis());
            return;
        }

        map.put(channelInfo.getChannel(), channelInfo);
        channelMap.put(channelInfo.getClientId(), channelInfo.getChannel());
        log.info("register producer, group: {}; channel: {};", groupName, channelInfo);
    }

    public void unregister(String groupName, ClientChannelInfo channelInfo) {
        ConcurrentMap<Channel, ClientChannelInfo> map = channelTree.get(groupName);
        if (MapUtil.isEmpty(map)) {
            return;
        }

        channelMap.remove(channelInfo.getClientId());
        ClientChannelInfo old = map.remove(channelInfo.getChannel());

        if (old != null) {
            log.info("unregister producer, group: {}; channel: {};", groupName, channelInfo);
            invokeListeners(ProducerEvent.CLIENT_UNREGISTER, groupName, channelInfo);
        }

        if (map.isEmpty()) {
            channelTree.remove(groupName);
            invokeListeners(ProducerEvent.GROUP_UNREGISTER, groupName, null);
            log.info("unregister group, group: {}", groupName);
        }
    }

    public int getGroupCount() {
        return channelTree.size();
    }

    public boolean isGroupExist(String groupName) {
        return MapUtil.notEmpty(
            channelTree.get(groupName)
        );
    }

    public void scanIdleChannels() {
    }

    private void invokeListeners(ProducerEvent event, String group, ClientChannelInfo channelInfo) {
        for (ProducerListener listener : listenerList) {
            try {
                listener.handle(event, group, channelInfo);
            } catch (Throwable t) {
                log.error("invoke producer listener error", t);
            }
        }
    }
}
