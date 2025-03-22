package cn.coderule.minimq.registry.domain.store.service;

import cn.coderule.minimq.registry.domain.store.StoreRegistry;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.model.StoreHealthInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import io.netty.channel.Channel;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelCloser {
    private final StoreRegistry registry;
    private final Route route;

    public ChannelCloser(StoreRegistry registry, Route route) {
        this.route = route;
        this.registry = registry;
    }

    public void close(Channel channel) {
        if (channel == null) {
            return;
        }

        UnRegisterBrokerRequestHeader request = null;

        try {
            route.lockRead();
            request = getUnregisterRequest(channel);
        } catch (Exception e) {
            log.error("close channel error", e);
        } finally {
            route.unlockRead();
        }

        if (request != null) {
            registry.unregisterAsync(request);
        }
    }

    private UnRegisterBrokerRequestHeader getUnregisterRequest(Channel channel) {
         StoreInfo store = getStoreInfo(channel);
        if (store == null) {
            return null;
        }
        UnRegisterBrokerRequestHeader request = toUnregisterRequestHeader(store);
        boolean exists = checkGroupInfo(request);

        return exists ? request : null;
    }

    private StoreInfo getStoreInfo(Channel channel) {
        for (Map.Entry<StoreInfo, StoreHealthInfo> entry: route.getHealthMap().entrySet()) {
            if (entry.getValue().getChannel() != channel) {
                continue;
            }
            return entry.getKey();
        }
        return null;
    }

    private UnRegisterBrokerRequestHeader toUnregisterRequestHeader(StoreInfo store) {
        UnRegisterBrokerRequestHeader requestHeader = new UnRegisterBrokerRequestHeader();
        requestHeader.setClusterName(store.getClusterName());
        requestHeader.setBrokerAddr(store.getAddress());

        return requestHeader;
    }

    private boolean checkGroupInfo(UnRegisterBrokerRequestHeader requestHeader) {
        for (Map.Entry<String, GroupInfo> entry : route.getGroupMap().entrySet()) {
            GroupInfo group = entry.getValue();

            if (!group.getCluster().equals(requestHeader.getClusterName())) {
                continue;
            }

            if (checkGroupAddress(group, requestHeader)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkGroupAddress(GroupInfo group, UnRegisterBrokerRequestHeader requestHeader) {
        for (Map.Entry<Long, String> entry : group.getBrokerAddrs().entrySet()) {
            long groupNo = entry.getKey();
            String address = entry.getValue();
            if (!address.equals(requestHeader.getBrokerAddr())) {
                continue;
            }

            requestHeader.setBrokerName(group.getBrokerName());
            requestHeader.setBrokerId(groupNo);
            return true;
        }

        return false;
    }
}
