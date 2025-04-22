package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.AddressScheme;
import apache.rocketmq.v2.Broker;
import apache.rocketmq.v2.Endpoints;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteConverter {
    public static QueryRouteResponse of(QueryRouteRequest request, RouteInfo routeInfo) {
        return null;
    }

    // brokerName -> brokerId -> Broker
    public static Map<String, Map<Long, Broker>> buildBrokerMap(RouteInfo routeInfo) {
        Map<String, Map<Long, Broker>> brokerMap = new HashMap<>();
        if (routeInfo == null) {
            return brokerMap;
        }

        for (GroupInfo groupInfo : routeInfo.getBrokerDatas()) {
            Map<Long, Broker> brokerIdMap = getBrokerIdMap(brokerMap, groupInfo.getBrokerName());
            putBrokerIdMap(brokerIdMap, groupInfo);
        }

        return brokerMap;
    }

    private static Map<Long, Broker> getBrokerIdMap(Map<String, Map<Long, Broker>> brokerMap, String groupName) {
        Map<Long, Broker> brokerIdMap;
        if (!brokerMap.containsKey(groupName)) {
            brokerIdMap = new HashMap<>();
            brokerMap.put(groupName, brokerIdMap);
        } else {
            brokerIdMap = brokerMap.get(groupName);
        }

        return brokerIdMap;
    }

    private static void putBrokerIdMap(Map<Long, Broker> brokerIdMap, GroupInfo groupInfo) {
        for (Map.Entry<Long, String> entry : groupInfo.getBrokerAddrs().entrySet()) {
            Broker broker = toBroker(entry, groupInfo.getBrokerName());
            brokerIdMap.put(entry.getKey(), broker);
        }
    }

    /**
     * @TODO use address list from registry
     *
     * @param entry GroupInfo.brokerAddrs.entry
     * @param groupName groupName
     * @return Broker
     */
    private static Broker toBroker(Map.Entry<Long, String> entry, String groupName) {
        String addrStr = entry.getValue();
        String[] addrArr = addrStr.split(";");
        List<Address> addrList = new ArrayList<>();
        for (String addr : addrArr) {
            Address address = Address.of(addr);
            addrList.add(address);
        }

        Endpoints endpoints = Endpoints.newBuilder()
            .setScheme(AddressScheme.IPv4)
            .addAllAddresses(toAddress(addrList))
            .build();

        return Broker.newBuilder()
            .setId(Math.toIntExact(entry.getKey()))
            .setName(groupName)
            .setEndpoints(endpoints)
            .build();
    }

    private static List<apache.rocketmq.v2.Address> toAddress(List<Address> addressList) {
        List<apache.rocketmq.v2.Address> result = new ArrayList<>();
        for (Address address : addressList) {
            result.add(toAddress(address));
        }

        return result;
    }

    private static apache.rocketmq.v2.Address toAddress(Address address) {
        return apache.rocketmq.v2.Address.newBuilder()
            .setHost(address.getHost())
            .setPort(address.getPort())
            .build();
    }


}
