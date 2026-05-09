package cn.coderule.wolfmq.broker.server.grpc.interceptor;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.SendMessageRequest;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestMappingTest {

    @Test
    void testMapQueryRouteRequest() {
        assertEquals(RequestCode.GET_ROUTEINFO_BY_TOPIC,
            RequestMapping.map(QueryRouteRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapHeartbeatRequest() {
        assertEquals(RequestCode.HEART_BEAT,
            RequestMapping.map(HeartbeatRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapSendMessageRequest() {
        assertEquals(RequestCode.SEND_MESSAGE_V2,
            RequestMapping.map(SendMessageRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapQueryAssignmentRequest() {
        assertEquals(RequestCode.GET_ROUTEINFO_BY_TOPIC,
            RequestMapping.map(QueryAssignmentRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapReceiveMessageRequest() {
        assertEquals(RequestCode.PULL_MESSAGE,
            RequestMapping.map(ReceiveMessageRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapAckMessageRequest() {
        assertEquals(RequestCode.UPDATE_CONSUMER_OFFSET,
            RequestMapping.map(AckMessageRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapEndTransactionRequest() {
        assertEquals(RequestCode.END_TRANSACTION,
            RequestMapping.map(EndTransactionRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapNotifyClientTerminationRequest() {
        assertEquals(RequestCode.UNREGISTER_CLIENT,
            RequestMapping.map(NotifyClientTerminationRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapChangeInvisibleDurationRequest() {
        assertEquals(RequestCode.CONSUMER_SEND_MSG_BACK,
            RequestMapping.map(ChangeInvisibleDurationRequest.getDescriptor().getFullName()));
    }

    @Test
    void testMapUnknownRequestReturnsDefault() {
        assertEquals(RequestCode.HEART_BEAT,
            RequestMapping.map("unknown.request.type"));
    }

    @Test
    void testMapNullReturnsDefault() {
        assertEquals(RequestCode.HEART_BEAT,
            RequestMapping.map(null));
    }

    @Test
    void testMapEmptyStringReturnsDefault() {
        assertEquals(RequestCode.HEART_BEAT,
            RequestMapping.map(""));
    }
}
