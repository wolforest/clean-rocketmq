package com.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.MapUtil;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.domain.store.model.Route;
import com.wolfmq.registry.domain.store.model.StoreStatusInfo;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.domain.server.registry.GroupInfo;
import com.wolfmq.rpc.registry.protocol.header.NotifyMinBrokerIdChangeRequestHeader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HaClient implements Lifecycle {
    private final RegistryConfig registryConfig;
    private final RpcClient rpcClient;

    public HaClient(RegistryConfig registryConfig, RpcClientConfig clientConfig) {
        this.registryConfig = registryConfig;
        this.rpcClient = new NettyClient(clientConfig);
    }

    @Override
    public void start() throws Exception {
        this.rpcClient.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.rpcClient.shutdown();
    }

    public void notifyMinIdChanged(Map<String, StoreStatusInfo> notifyMap, Route route) throws Exception {
        if (MapUtil.isEmpty(notifyMap)) {
            return;
        }

        if (!registryConfig.isNotifyMinIdChanged()) {
            return;
        }

        for (Map.Entry<String, StoreStatusInfo> entry : notifyMap.entrySet()) {
            StoreStatusInfo statusInfo = entry.getValue();
            GroupInfo groupInfo = route.getGroup(entry.getKey());
            if (null == groupInfo) {
                continue;
            }

            if (!groupInfo.isEnableActingMaster()) {
                continue;
            }

            notifyMinIdChanged(groupInfo, statusInfo.getHaBrokerAddr(), statusInfo.getOfflineBrokerAddr());
        }
    }

    public void notifyMinIdChanged(GroupInfo group, String haAddr) throws Exception {
        notifyMinIdChanged(group, haAddr, null);
    }

    public void notifyMinIdChanged(GroupInfo group, String haAddr, String offlineAddr) throws Exception {
        if (!needNotifyMinIdChanged(group)) {
            return;
        }

        NotifyMinBrokerIdChangeRequestHeader requestHeader = new NotifyMinBrokerIdChangeRequestHeader();
        long minNo = group.getMinNo();
        requestHeader.setMinBrokerId(minNo);
        requestHeader.setMinBrokerAddr(group.getAddress(minNo));
        requestHeader.setOfflineBrokerAddr(offlineAddr);
        requestHeader.setHaBrokerAddr(haAddr);

        List<String> addrList = chooseBrokerAddrsToNotify(group.getBrokerAddrs(), offlineAddr);
        log.info("NotifyMinBrokerIdChangeRequestHeader: {}, notify address list: {}", requestHeader, addrList);

        RpcCommand request = RpcCommand.createRequestCommand(RequestCode.NOTIFY_MIN_BROKER_ID_CHANGE, requestHeader);
        for (String addr : addrList) {
            rpcClient.invokeOneway(addr, request, 5000);
        }
    }

    private boolean needNotifyMinIdChanged(GroupInfo group) {
        if (!registryConfig.isNotifyMinIdChanged()) {
            return false;
        }

        return null != group && !MapUtil.isEmpty(group.getBrokerAddrs());
    }

    private List<String> chooseBrokerAddrsToNotify(Map<Long, String> addrMap, String offlineAddr) {
        if (offlineAddr != null || addrMap.size() == 1) {
            // notify the reset servers.
            return new ArrayList<>(addrMap.values());
        }

        // new server registered, notify previous servers.
        long minId = Collections.min(addrMap.keySet());
        List<String> addrList = new ArrayList<>();
        for (Long brokerId : addrMap.keySet()) {
            if (brokerId != minId) {
                addrList.add(addrMap.get(brokerId));
            }
        }
        return addrList;
    }
}
