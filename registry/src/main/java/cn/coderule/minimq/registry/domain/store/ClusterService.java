package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.constant.MQConstants;
import cn.coderule.minimq.domain.constant.PermName;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.model.StoreHealthInfo;
import cn.coderule.minimq.registry.domain.store.model.StoreStatusInfo;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.registry.protocol.body.BrokerMemberGroup;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigAndMappingSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.NotifyMinBrokerIdChangeRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.statictopic.TopicQueueMappingInfo;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterService {
    private static final long DEFAULT_BROKER_CHANNEL_EXPIRED_TIME = 1000 * 60 * 2;

    private final RegistryConfig config;
    private final Route route;

    public ClusterService(RegistryConfig config, Route route) {
        this.route = route;
        this.config = config;
    }

    public DataVersion getStoreVersion(StoreInfo store) {
        return route.getHealthVersion(store);
    }

    public ClusterInfo getClusterInfo() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setBrokerAddrTable(route.getGroupMap());
        clusterInfo.setClusterAddrTable(route.getClusterMap());
        return clusterInfo;
    }

    public BrokerMemberGroup getGroupInfo(String clusterName, String groupName) {
        BrokerMemberGroup memberGroup = new BrokerMemberGroup(clusterName, groupName);

        try {
            route.lockRead();
            GroupInfo groupInfo = route.getGroup(groupName);
            if (groupInfo != null) {
                memberGroup.setBrokerAddrs(groupInfo.getBrokerAddrs());
            }
        } catch (Exception e) {
            log.error("get group info error", e);
        } finally {
            route.unlockRead();
        }

        return memberGroup;
    }

    public void flushStoreUpdateTime(String clusterName, String address) {
        StoreInfo store = new StoreInfo(clusterName, address);
        StoreHealthInfo healthInfo = route.getHealthInfo(store);
        if (healthInfo == null) {
            return;
        }

        healthInfo.setLastUpdateTimestamp(System.currentTimeMillis());
    }

    public int removeGroupWritePermission(String groupName) {
        return 0;
    }

    public int addGroupWritePermission(String groupName) {
        return 0;
    }

}
