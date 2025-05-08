package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;

public class AssignmentConverter {
    public static QueryAssignmentResponse of(QueryAssignmentRequest request, RouteInfo routeInfo) {

        return null;
    }
}
