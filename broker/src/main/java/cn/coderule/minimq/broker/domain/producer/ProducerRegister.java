package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.lang.concurrent.atomic.PositiveAtomicCounter;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.enums.produce.ProducerEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.service.broker.listener.ProducerListener;
import cn.coderule.minimq.rpc.broker.rpc.protocol.body.ProducerInfo;
import cn.coderule.minimq.rpc.broker.rpc.protocol.body.ProducerTableInfo;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyHelper;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProducerRegister {
    private final long channelExpireTime;
    private final int maxChannelFetchTimes;

    private final PositiveAtomicCounter counter;
    private final List<ProducerListener> listenerList;
    // clientId -> channel
    private final ConcurrentMap<String, Channel> channelMap;
    // groupName -> channel -> channelInfo
    private final ConcurrentMap<String, ConcurrentMap<Channel, ClientChannelInfo>> channelTree;

    public ProducerRegister(BrokerConfig brokerConfig) {
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

    public synchronized boolean invokeCloseEvent(final String remoteAddr, final Channel channel) {
        boolean removed = false;
        if (channel == null) {
            return false;
        }

        for (final Map.Entry<String, ConcurrentMap<Channel, ClientChannelInfo>> entry : this.channelTree.entrySet()) {
            final String group = entry.getKey();
            final ConcurrentMap<Channel, ClientChannelInfo> infoMap = entry.getValue();
            final ClientChannelInfo channelInfo = infoMap.remove(channel);
            if (channelInfo == null) {
                continue;
            }

            channelMap.remove(channelInfo.getClientId());
            removed = true;
            log.info("NETTY EVENT: remove channel[{}][{}] from ProducerManager groupChannelTable, producer group: {}",
                channelInfo, remoteAddr, group);
            invokeListeners(ProducerEvent.CLIENT_UNREGISTER, group, channelInfo);

            if (!infoMap.isEmpty()) {
                continue;
            }

            ConcurrentMap<Channel, ClientChannelInfo> old = this.channelTree.remove(group);
            if (old == null) {
                continue;
            }

            log.info("unregister a producer group[{}] from groupChannelTable", group);
            invokeListeners(ProducerEvent.GROUP_UNREGISTER, group, null);
        }

        return removed;
    }

    public void addListener(ProducerListener listener) {
        this.listenerList.add(listener);
    }

    public Channel getAvailableChannel(String groupName) {
        List<Channel> channelList = getChannelList(groupName);
        if (channelList.isEmpty()) {
            return null;
        }

        return filterAvailableChannel(channelList);
    }

    public int getGroupCount() {
        return channelTree.size();
    }

    public boolean isGroupExist(String groupName) {
        return MapUtil.notEmpty(
            channelTree.get(groupName)
        );
    }

    public ProducerTableInfo getProducerTable() {
        Map<String, List<ProducerInfo>> map = new HashMap<>();
        for (String group : this.channelTree.keySet()) {
            getProducerTable(map, group);
        }
        return new ProducerTableInfo(map);
    }

    public void scanIdleChannels() {
        Iterator<Map.Entry<String, ConcurrentMap<Channel, ClientChannelInfo>>> iterator
            = this.channelTree.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentMap<Channel, ClientChannelInfo>> entry = iterator.next();

            final String group = entry.getKey();
            final ConcurrentMap<Channel, ClientChannelInfo> chlMap = entry.getValue();

            scanIdleChannels(chlMap, group);

            if (!chlMap.isEmpty()) {
                continue;
            }

            log.warn("SCAN: remove expired channel from ProducerManager groupChannelTable, all clear, group={}", group);
            iterator.remove();
            invokeListeners(ProducerEvent.GROUP_UNREGISTER, group, null);
        }
    }

    private void scanIdleChannels(ConcurrentMap<Channel, ClientChannelInfo> chlMap, String group) {
        Iterator<Map.Entry<Channel, ClientChannelInfo>> it = chlMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Channel, ClientChannelInfo> item = it.next();
            // final Integer id = item.getKey();
            final ClientChannelInfo info = item.getValue();

            long diff = System.currentTimeMillis() - info.getLastUpdateTime();
            if (diff <= channelExpireTime) {
                continue;
            }

            it.remove();
            Channel channelInClientTable = channelMap.get(info.getClientId());
            if (channelInClientTable != null && channelInClientTable.equals(info.getChannel())) {
                channelMap.remove(info.getClientId());
            }
            log.warn("ProducerManager#scanIdleChannels: remove expired channel[{}] groupName: {}",
                NettyHelper.getRemoteAddr(info.getChannel()), group);

            invokeListeners(ProducerEvent.CLIENT_UNREGISTER, group, info);
            NettyHelper.close(info.getChannel());
        }
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

    private ProducerInfo createProducerInfo(ClientChannelInfo clientChannelInfo) {
        return new ProducerInfo(
            clientChannelInfo.getClientId(),
            clientChannelInfo.getChannel().remoteAddress().toString(),
            clientChannelInfo.getLanguage(),
            clientChannelInfo.getVersion(),
            clientChannelInfo.getLastUpdateTime()
        );
    }

    private void getProducerTable(Map<String, List<ProducerInfo>> map, String group) {
        for (Map.Entry<Channel, ClientChannelInfo> entry: this.channelTree.get(group).entrySet()) {
            ClientChannelInfo channelInfo = entry.getValue();
            if (map.containsKey(group)) {
                map.get(group).add(createProducerInfo(channelInfo));
                continue;
            }

            map.put(group, new ArrayList<>(Collections.singleton(createProducerInfo(channelInfo))));
        }
    }

    private List<Channel> getChannelList(String groupName) {
        if (groupName == null) {
            return List.of();
        }

        ConcurrentMap<Channel, ClientChannelInfo> map = channelTree.get(groupName);
        if (MapUtil.isEmpty(map)) {
            log.warn("getAvailableChannel failed, channel table is empty. groupId={}", groupName);
            return List.of();
        }

        return new ArrayList<>(map.keySet());
    }

    private Channel filterAvailableChannel(List<Channel> channelList) {
        int count = 0;
        Channel lastActiveChannel = null;
        int index = counter.incrementAndGet() % channelList.size();

        while (count++ < maxChannelFetchTimes) {
            index = (++index) % channelList.size();
            Channel channel = channelList.get(index);
            boolean isOk = channel.isActive() && channel.isWritable();

            if (isOk) {
                return channel;
            }

            if (channel.isActive()) {
                lastActiveChannel = channel;
            }
        }

        return lastActiveChannel;
    }

}
