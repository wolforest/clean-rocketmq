package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.AddressScheme;
import apache.rocketmq.v2.Broker;
import apache.rocketmq.v2.Endpoints;
import apache.rocketmq.v2.MessageQueue;
import apache.rocketmq.v2.MessageType;
import apache.rocketmq.v2.Permission;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Resource;
import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.domain.domain.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static void addReadOnlyQueue(
        List<MessageQueue> messageQueueList,
        Resource topic,
        cn.coderule.minimq.domain.core.enums.message.MessageType messageType,
        Broker broker,
        int num
    ) {
        int counter = 0;
        for (int i = 0; i < num; i++) {
            MessageQueue messageQueue = MessageQueue.newBuilder()
                .setBroker(broker)
                .setTopic(topic)
                .setId(counter++)
                .setPermission(Permission.READ)
                .addAllAcceptMessageTypes(toTypeList(messageType))
                .build();
            messageQueueList.add(messageQueue);
        }

    }

    private static void addWriteOnlyQueue(
        List<MessageQueue> messageQueueList,
        Resource topic,
        cn.coderule.minimq.domain.core.enums.message.MessageType messageType,
        Broker broker,
        int num
    ) {
        int counter = 0;
        for (int i = 0; i < num; i++) {
            MessageQueue messageQueue = MessageQueue.newBuilder().setBroker(broker).setTopic(topic)
                .setId(counter++)
                .setPermission(Permission.WRITE)
                .addAllAcceptMessageTypes(toTypeList(messageType))
                .build();
            messageQueueList.add(messageQueue);
        }
    }

    private static void addReadWriteQueue(
        List<MessageQueue> messageQueueList,
        Resource topic,
        cn.coderule.minimq.domain.core.enums.message.MessageType messageType,
        Broker broker,
        int num
    ) {
        int counter = 0;
        for (int i = 0; i < num; i++) {
            MessageQueue messageQueue = MessageQueue.newBuilder().setBroker(broker).setTopic(topic)
                .setId(counter++)
                .setPermission(Permission.READ_WRITE)
                .addAllAcceptMessageTypes(toTypeList(messageType))
                .build();
            messageQueueList.add(messageQueue);
        }
    }

    public static List<MessageType> toTypeList(cn.coderule.minimq.domain.core.enums.message.MessageType type) {
        return switch (type) {
            case NORMAL -> Collections.singletonList(MessageType.NORMAL);
            case ORDER -> Collections.singletonList(MessageType.FIFO);
            case TRANSACTION -> Collections.singletonList(MessageType.TRANSACTION);
            case DELAY -> Collections.singletonList(MessageType.DELAY);
            case MIXED -> Arrays.asList(
                MessageType.NORMAL,
                MessageType.FIFO,
                MessageType.DELAY,
                MessageType.TRANSACTION
            );
            default -> Collections.singletonList(MessageType.MESSAGE_TYPE_UNSPECIFIED);
        };
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
