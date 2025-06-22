package cn.coderule.minimq.registry.domain.store.service;

import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.StoreRegistry;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.model.StoreHealthInfo;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyHelper;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdleScanner {
    private final RegistryConfig config;
    private final StoreRegistry registry;
    private final Route route;
    private final ScheduledExecutorService scheduler;

    public IdleScanner(RegistryConfig config, StoreRegistry registry, Route route) {
        this.config = config;

        this.route = route;
        this.registry = registry;

        this.scheduler = initScheduler();
    }

    public void start() {
        this.scheduler.scheduleAtFixedRate(
            IdleScanner.this::scan,
            5,
            config.getIdleScanInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    public void shutdown() {
        this.scheduler.shutdown();
    }

    public void scan() {
        try {
            log.info("start scan idle nodes.");
            for (Map.Entry<StoreInfo, StoreHealthInfo> entry: route.getHealthMap().entrySet()) {
                long last = entry.getValue().getLastUpdateTimestamp();
                long timeout  = entry.getValue().getHeartbeatTimeoutMillis();
                if ( (last + timeout) >= System.currentTimeMillis()) {
                    continue;
                }

                NettyHelper.close(entry.getValue().getChannel());
                log.warn("the channel expired, {}, {}ms", entry.getKey(), timeout);

                onChannelClose(entry.getKey());
            }
        } catch (Exception e) {
            log.error("scan idle nodes exception", e);
        }
    }

    private void onChannelClose(StoreInfo store) {
        if (store == null) {
            return;
        }

        UnRegisterBrokerRequestHeader requestHeader = toUnregisterRequestHeader(store);

        boolean shouldUnregister = shouldUnregister(requestHeader);
        if (!shouldUnregister) {
            return;
        }

        boolean status = registry.unregisterAsync(requestHeader);
        log.info("the channel was closed, submit the unregister request, broker: {}, submit result: {}",
            store, status
        );
    }

    private UnRegisterBrokerRequestHeader toUnregisterRequestHeader(StoreInfo store) {
        UnRegisterBrokerRequestHeader requestHeader = new UnRegisterBrokerRequestHeader();
        requestHeader.setClusterName(store.getClusterName());
        requestHeader.setBrokerAddr(store.getAddress());

        return requestHeader;
    }

    private boolean shouldUnregister(UnRegisterBrokerRequestHeader requestHeader) {
        boolean shouldUnregister = false;

        try {
            route.lockRead();
            shouldUnregister = checkGroupInfo(requestHeader);
        } catch (Exception e) {
            log.error("onChannelClose error", e);
        } finally {
            route.unlockRead();
        }

        return shouldUnregister;
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
    private ScheduledExecutorService initScheduler() {
        return ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("IdleScanner")
        );
    }
}
