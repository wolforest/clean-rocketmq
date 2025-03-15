package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreRegistry {
    private final RegistryConfig config;
    private final Route route;

    private final RpcClient rpcClient;
    private final UnregisterService unregisterService;

    public StoreRegistry(RegistryConfig config, RpcClient rpcClient, Route route) {
        this.route = route;
        this.config = config;

        this.rpcClient = rpcClient;
        this.unregisterService = new UnregisterService(config, this);
    }

    public void start() {
        unregisterService.start();
    }

    public void shutdown() {
        unregisterService.shutdown();
    }

    private GroupInfo getOrCreateGroup(StoreInfo storeInfo) {
        return route.getOrCreateGroup(
            storeInfo.getZoneName(),
            storeInfo.getClusterName(),
            storeInfo.getGroupName(),
            storeInfo.isEnableActingMaster()
        );
    }

    private boolean checkMinIdChanged(StoreInfo store, GroupInfo group) {
        Map<Long, String> addrMap = group.getBrokerAddrs();

        boolean isMinIdChanged = false;
        long preMinId = 0;
        if (!addrMap.isEmpty()) {
            preMinId = Collections.min(addrMap.keySet());
        }

        if (store.getGroupNo() < preMinId) {
            isMinIdChanged = true;
        }

        return isMinIdChanged;
    }

    private void removeExistAddress(StoreInfo store, GroupInfo group) {
        Map<Long, String> addrMap = group.getBrokerAddrs();

        //Switch slave to master: first remove <1, IP:PORT> in namesrv, then add <0, IP:PORT>
        //The same IP:PORT must only have one record in brokerAddrTable
        addrMap.entrySet().removeIf(
            item -> null != store.getAddress()
                && store.getAddress().equals(item.getValue())
                && store.getGroupNo() != item.getKey()
        );
    }

    private boolean checkHealthInfo(StoreInfo store, GroupInfo group, RouteInfo routeInfo) {
        Map<Long, String> addrMap = group.getBrokerAddrs();
        String oldAddr = addrMap.get(store.getGroupNo());


        return true;
    }

    public StoreRegisterResult register(StoreInfo store, RouteInfo routeInfo, List<String> filterList) {
        StoreRegisterResult result = new StoreRegisterResult();
        try {
            route.lockWrite();

            GroupInfo group = getOrCreateGroup(store);
            boolean isMinIdChanged = checkMinIdChanged(store, group);
            removeExistAddress(store, group);

            if (!checkHealthInfo(store, group, routeInfo)) {
                return result;
            }



        } catch (Exception e) {
            log.error("register store error", e);
        } finally {
            route.unlockWrite();
        }

        return result;
    }

    public void unregister(Set<UnRegisterBrokerRequestHeader> requests) {

    }

    public boolean unregisterAsync(UnRegisterBrokerRequestHeader request) {
        return unregisterService.submit(request);
    }
}
