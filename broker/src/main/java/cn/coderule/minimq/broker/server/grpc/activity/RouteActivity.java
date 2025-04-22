package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.AddressScheme;
import apache.rocketmq.v2.Broker;
import apache.rocketmq.v2.Endpoints;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.domain.config.GrpcConfig;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

public class RouteActivity {
    private final GrpcConfig grpcConfig;
    private final ThreadPoolExecutor executor;

    @Setter
    private RouteController routeController;

    public RouteActivity(GrpcConfig grpcConfig, ThreadPoolExecutor executor) {
        this.grpcConfig = grpcConfig;
        this.executor = executor;
    }

    public void getRoute(RequestContext context, QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
        ActivityHelper<QueryRouteRequest, QueryRouteResponse> helper = getRouteHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getRouteAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void getAssignment(RequestContext context, QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
        ActivityHelper<QueryAssignmentRequest, QueryAssignmentResponse> helper = getAssignmentHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getAssignmentAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    private ActivityHelper<QueryAssignmentRequest, QueryAssignmentResponse> getAssignmentHelper(
        RequestContext context,
        QueryAssignmentRequest request,
        StreamObserver<QueryAssignmentResponse> responseObserver
    ) {
        Function<Status, QueryAssignmentResponse> statusToResponse = assignmentStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private ActivityHelper<QueryRouteRequest, QueryRouteResponse> getRouteHelper(
        RequestContext context,
        QueryRouteRequest request,
        StreamObserver<QueryRouteResponse> responseObserver
    ) {
        Function<Status, QueryRouteResponse> statusToResponse = routeStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private CompletableFuture<QueryRouteResponse> getAssignmentAsync(RequestContext context, QueryAssignmentRequest request) {
        //routeController.getRoute(context, request.getTopic().getName());
        return CompletableFuture.completedFuture(null);
    }


    private CompletableFuture<QueryRouteResponse> getRouteAsync(RequestContext context, QueryRouteRequest request) {
        String topicName = request.getTopic().getName();
        List<Address> addressList = toAddressList(request.getEndpoints());
        context.setServerPort(grpcConfig.getPort());

        return routeController.getRoute(context, topicName, addressList)
            .thenApply(routeInfo -> {
                Map<String, Map<Long, Broker>> brokerMap = buildBrokerMap(routeInfo);
                return null;
            });
    }


    // brokerName -> brokerId -> Broker
    private Map<String, Map<Long, Broker>> buildBrokerMap(RouteInfo routeInfo) {
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

    private Map<Long, Broker> getBrokerIdMap(Map<String, Map<Long, Broker>> brokerMap, String groupName) {
        Map<Long, Broker> brokerIdMap;
        if (!brokerMap.containsKey(groupName)) {
            brokerIdMap = new HashMap<>();
            brokerMap.put(groupName, brokerIdMap);
        } else {
            brokerIdMap = brokerMap.get(groupName);
        }

        return brokerIdMap;
    }

    private void putBrokerIdMap(Map<Long, Broker> brokerIdMap, GroupInfo groupInfo) {
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
    private Broker toBroker(Map.Entry<Long, String> entry, String groupName) {
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

    private List<apache.rocketmq.v2.Address> toAddress(List<Address> addressList) {
        List<apache.rocketmq.v2.Address> result = new ArrayList<>();
        for (Address address : addressList) {
            result.add(toAddress(address));
        }

        return result;
    }

    private apache.rocketmq.v2.Address toAddress(Address address) {
        return apache.rocketmq.v2.Address.newBuilder()
            .setHost(address.getHost())
            .setPort(address.getPort())
            .build();
    }

    private List<Address> toAddressList(Endpoints endpoints) {
        List<Address> addressList = new ArrayList<>();
        for (apache.rocketmq.v2.Address address : endpoints.getAddressesList()) {
            addressList.add(Address.of(address.getHost(), grpcConfig.getPort()));
        }

        return addressList;
    }

    private Function<Status, QueryRouteResponse> routeStatueToResponse() {
        return status -> QueryRouteResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private Function<Status, QueryAssignmentResponse> assignmentStatueToResponse() {
        return status -> QueryAssignmentResponse.newBuilder()
            .setStatus(status)
            .build();
    }
}
