package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.AddressScheme;
import apache.rocketmq.v2.Assignment;
import apache.rocketmq.v2.MessageQueue;
import apache.rocketmq.v2.MessageType;
import apache.rocketmq.v2.Permission;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Resource;
import cn.coderule.wolfmq.domain.core.enums.message.MessageType;
import cn.coderule.wolfmq.domain.domain.cluster.route.QueueInfo;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.wolfmq.domain.domain.cluster.server.GroupInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouteConverterTest {

    private QueryAssignmentRequest buildAssignmentRequest(String topic) {
        return QueryAssignmentRequest.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .build();
    }

    private QueryRouteRequest buildRouteRequest(String topic) {
        return QueryRouteRequest.newBuilder()
            .setTopic(Resource.newBuilder().setName(topic).build())
            .build();
    }

    private QueueInfo createQueueInfo(String brokerName, int readNums, int writeNums, int perm) {
        QueueInfo qi = new QueueInfo();
        qi.setBrokerName(brokerName);
        qi.setReadQueueNums(readNums);
        qi.setWriteQueueNums(writeNums);
        qi.setPerm(perm);
        qi.setMessageType(cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL);
        return qi;
    }

    private GroupInfo createGroupInfo(String brokerName, long brokerId, String address) {
        GroupInfo gi = new GroupInfo();
        gi.setBrokerName(brokerName);
        gi.setCluster("DefaultCluster");
        Map<Long, String> addrs = new HashMap<>();
        addrs.put(brokerId, address);
        gi.setBrokerAddrs(addrs);
        return gi;
    }

    private RouteInfo createRouteInfo(List<QueueInfo> queueDatas, List<GroupInfo> brokerDatas) {
        RouteInfo ri = new RouteInfo();
        ri.setQueueDatas(queueDatas);
        ri.setBrokerDatas(brokerDatas);
        ri.setTopicName("test_topic");
        ri.setMessageType(cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL);
        return ri;
    }

    // ===== toTypeList tests =====

    @Test
    void toTypeList_normal_returnsNormalList() {
        List<MessageType> types = RouteConverter.toTypeList(
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL);
        assertEquals(1, types.size());
        assertEquals(MessageType.NORMAL, types.get(0));
    }

    @Test
    void toTypeList_fifo_returnsFifoList() {
        List<MessageType> types = RouteConverter.toTypeList(
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.FIFO);
        assertEquals(1, types.size());
        assertEquals(MessageType.FIFO, types.get(0));
    }

    @Test
    void toTypeList_transaction_returnsTransactionList() {
        List<MessageType> types = RouteConverter.toTypeList(
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.TRANSACTION);
        assertEquals(1, types.size());
        assertEquals(MessageType.TRANSACTION, types.get(0));
    }

    @Test
    void toTypeList_delay_returnsDelayList() {
        List<MessageType> types = RouteConverter.toTypeList(
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.DELAY);
        assertEquals(1, types.size());
        assertEquals(MessageType.DELAY, types.get(0));
    }

    @Test
    void toTypeList_mixed_returnsAllTypes() {
        List<MessageType> types = RouteConverter.toTypeList(
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.MIXED);
        assertEquals(4, types.size());
        assertTrue(types.contains(MessageType.NORMAL));
        assertTrue(types.contains(MessageType.FIFO));
        assertTrue(types.contains(MessageType.DELAY));
        assertTrue(types.contains(MessageType.TRANSACTION));
    }

    // ===== toAssignmentResponse tests =====

    @Test
    void toAssignmentResponse_withReadableQueue_returnsAssignments() {
        // 6 = readable + writable (PermName.PERM_READ | PermName.PERM_WRITE)
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 6);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryAssignmentResponse response = RouteConverter.toAssignmentResponse(
            routeInfo, false, buildAssignmentRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        assertFalse(response.getAssignmentsList().isEmpty());
    }

    @Test
    void toAssignmentResponse_fifo_returnsPerQueueAssignment() {
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 6);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryAssignmentResponse response = RouteConverter.toAssignmentResponse(
            routeInfo, true, buildAssignmentRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        // FIFO mode: one assignment per read queue
        assertEquals(4, response.getAssignmentsList().size());
    }

    @Test
    void toAssignmentResponse_nonFifo_returnsSingleAssignment() {
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 6);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryAssignmentResponse response = RouteConverter.toAssignmentResponse(
            routeInfo, false, buildAssignmentRequest("test_topic"));

        // non-FIFO: single assignment with id=-1
        assertEquals(1, response.getAssignmentsList().size());
    }

    @Test
    void toAssignmentResponse_noReadableQueue_returnsForbidden() {
        // perm=2 = write-only (not readable)
        QueueInfo qi = createQueueInfo("broker-a", 0, 4, 2);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryAssignmentResponse response = RouteConverter.toAssignmentResponse(
            routeInfo, false, buildAssignmentRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.FORBIDDEN, response.getStatus().getCode());
        assertTrue(response.getAssignmentsList().isEmpty());
    }

    @Test
    void toAssignmentResponse_zeroReadQueueNums_returnsForbidden() {
        QueueInfo qi = createQueueInfo("broker-a", 0, 4, 6);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryAssignmentResponse response = RouteConverter.toAssignmentResponse(
            routeInfo, false, buildAssignmentRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.FORBIDDEN, response.getStatus().getCode());
    }

    // ===== toRouteResponse tests =====

    @Test
    void toRouteResponse_withReadWritePerm_returnsReadWriteQueues() {
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 6);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryRouteResponse response = RouteConverter.toRouteResponse(
            routeInfo,
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL,
            buildRouteRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        assertFalse(response.getMessageQueuesList().isEmpty());
    }

    @Test
    void toRouteResponse_writeOnlyPerm_returnsWriteOnlyQueues() {
        // perm=2 = write only
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 2);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryRouteResponse response = RouteConverter.toRouteResponse(
            routeInfo,
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL,
            buildRouteRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        // write-only queues
        for (MessageQueue mq : response.getMessageQueuesList()) {
            assertEquals(Permission.WRITE, mq.getPermission());
        }
    }

    @Test
    void toRouteResponse_readOnlyPerm_returnsReadOnlyQueues() {
        // perm=4 = read only
        QueueInfo qi = createQueueInfo("broker-a", 4, 0, 4);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryRouteResponse response = RouteConverter.toRouteResponse(
            routeInfo,
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL,
            buildRouteRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        for (MessageQueue mq : response.getMessageQueuesList()) {
            assertEquals(Permission.READ, mq.getPermission());
        }
    }

    @Test
    void toRouteResponse_multipleBrokers_returnsAllQueues() {
        QueueInfo qi1 = createQueueInfo("broker-a", 4, 4, 6);
        QueueInfo qi2 = createQueueInfo("broker-b", 2, 2, 6);
        GroupInfo gi1 = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        GroupInfo gi2 = createGroupInfo("broker-b", 0L, "127.0.0.1:10912");
        RouteInfo routeInfo = createRouteInfo(List.of(qi1, qi2), List.of(gi1, gi2));

        QueryRouteResponse response = RouteConverter.toRouteResponse(
            routeInfo,
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL,
            buildRouteRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        // Should have queues from both brokers: 4+4 (rw) from broker-a, 2+2 (rw) from broker-b
        assertTrue(response.getMessageQueuesList().size() >= 4);
    }

    @Test
    void toRouteResponse_fifoMessageType_returnsFifoQueue() {
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 6);
        GroupInfo gi = createGroupInfo("broker-a", 0L, "127.0.0.1:10911");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryRouteResponse response = RouteConverter.toRouteResponse(
            routeInfo,
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.FIFO,
            buildRouteRequest("test_topic"));

        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
        // Each queue should accept FIFO message type
        for (MessageQueue mq : response.getMessageQueuesList()) {
            assertTrue(mq.getAcceptMessageTypesList().contains(MessageType.FIFO));
        }
    }

    @Test
    void toRouteResponse_noMatchBroker_skipsQueue() {
        // QueueInfo references broker-a but brokerDatas has broker-b
        QueueInfo qi = createQueueInfo("broker-a", 4, 4, 6);
        GroupInfo gi = createGroupInfo("broker-b", 0L, "127.0.0.1:10912");
        RouteInfo routeInfo = createRouteInfo(List.of(qi), List.of(gi));

        QueryRouteResponse response = RouteConverter.toRouteResponse(
            routeInfo,
            cn.coderule.wolfmq.domain.core.enums.message.MessageType.NORMAL,
            buildRouteRequest("test_topic"));

        // Should be empty because broker-a has no matching group info
        assertTrue(response.getMessageQueuesList().isEmpty());
    }
}