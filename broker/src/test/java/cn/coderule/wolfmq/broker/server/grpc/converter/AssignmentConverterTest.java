package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignmentConverterTest {

    @Test
    void of_ShouldReturnNull() {
        QueryAssignmentRequest request = mock(QueryAssignmentRequest.class);
        RouteInfo routeInfo = mock(RouteInfo.class);
        QueryAssignmentResponse response = AssignmentConverter.of(request, routeInfo);
        assertNull(response);
    }
}
