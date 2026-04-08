package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;

public class AssignmentConverter {
    public static QueryAssignmentResponse of(QueryAssignmentRequest request, RouteInfo routeInfo) {

        return null;
    }
}
