package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;

public class RouteConverter {
    public static QueryRouteResponse of(QueryRouteRequest request, RouteInfo routeInfo) {
        return null;
    }

}
