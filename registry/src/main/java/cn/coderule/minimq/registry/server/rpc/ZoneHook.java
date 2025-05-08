package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.domain.constant.MQConstants;
import cn.coderule.minimq.rpc.common.rpc.RpcHook;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.route.QueueInfo;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZoneHook implements RpcHook {
    @Override
    public void onRequestStart(RpcContext ctx, RpcCommand request) {

    }

    @Override
    public void onResponseComplete(RpcContext ctx, RpcCommand request, RpcCommand response) {
        if (RequestCode.GET_ROUTEINFO_BY_TOPIC != request.getCode()) {
            return;
        }
        if (response == null || response.getBody() == null || ResponseCode.SUCCESS != response.getCode()) {
            return;
        }
        boolean zoneMode = Boolean.parseBoolean(request.getExtFields().get(MQConstants.ZONE_MODE));
        if (!zoneMode) {
            return;
        }
        String zoneName = request.getExtFields().get(MQConstants.ZONE_NAME);
        if (StringUtil.isBlank(zoneName)) {
            return;
        }

        RouteInfo topicRouteData = RpcSerializable.decode(response.getBody(), RouteInfo.class);
        response.setBody(filterByZone(topicRouteData, zoneName).encode());
    }

    private RouteInfo filterByZone(RouteInfo topicRouteData, String zoneName) {
        Map<String, GroupInfo> brokerDataRemoved = filterGroupInfo(topicRouteData, zoneName);

        filterQueueInfo(topicRouteData, brokerDataRemoved);
        filterFilterInfo(topicRouteData, brokerDataRemoved);

        return topicRouteData;
    }

    private Map<String, GroupInfo> filterGroupInfo(RouteInfo topicRouteData, String zoneName) {
        List<GroupInfo> groupInfoReserved = new ArrayList<>();
        Map<String, GroupInfo> brokerDataRemoved = new HashMap<>();
        for (GroupInfo groupInfo : topicRouteData.getBrokerDatas()) {
            //master down, consume from slave. break nearby route rule.
            if (groupInfo.getBrokerAddrs().get(MQConstants.MASTER_ID) == null
                || StringUtil.equalsIgnoreCase(groupInfo.getZoneName(), zoneName)) {
                groupInfoReserved.add(groupInfo);
            } else {
                brokerDataRemoved.put(groupInfo.getBrokerName(), groupInfo);
            }
        }
        topicRouteData.setBrokerDatas(groupInfoReserved);

        return brokerDataRemoved;
    }

    private void filterQueueInfo(RouteInfo topicRouteData, Map<String, GroupInfo> brokerDataRemoved) {
        List<QueueInfo> queueDataReserved = new ArrayList<>();
        for (QueueInfo queueData : topicRouteData.getQueueDatas()) {
            if (!brokerDataRemoved.containsKey(queueData.getBrokerName())) {
                queueDataReserved.add(queueData);
            }
        }
        topicRouteData.setQueueDatas(queueDataReserved);
    }

    private void filterFilterInfo(RouteInfo routeInfo, Map<String, GroupInfo> groupRemoved) {
        if (MapUtil.isEmpty(routeInfo.getFilterServerTable())) {
            return;
        }

        // remove filter server table by broker address
        for (Map.Entry<String, GroupInfo> entry : groupRemoved.entrySet()) {
            GroupInfo groupInfo = entry.getValue();
            if (groupInfo.getBrokerAddrs() == null) {
                continue;
            }
            groupInfo.getBrokerAddrs().values()
                .forEach(brokerAddr -> routeInfo.getFilterServerTable().remove(brokerAddr));
        }
    }



}
