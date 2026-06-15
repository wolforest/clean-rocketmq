package cn.coderule.wolfmq.rpc.registry.route;

import cn.coderule.wolfmq.domain.domain.cluster.route.QueueInfo;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteConverterTest {

    @Test
    void getQueueSet_WithReadableQueueData_ShouldReturnQueueSet() {
        String topic = "test-topic";
        RouteInfo route = mock(RouteInfo.class);
        when(route.isQueueMappingEmpty()).thenReturn(true);

        List<QueueInfo> queueDatas = new ArrayList<>();
        QueueInfo qi = mock(QueueInfo.class);
        when(qi.getBrokerName()).thenReturn("broker-a");
        when(qi.getPerm()).thenReturn(6);
        when(qi.getReadQueueNums()).thenReturn(4);
        queueDatas.add(qi);
        when(route.getQueueDatas()).thenReturn(queueDatas);

        Set<MessageQueue> result = RouteConverter.getQueueSet(topic, route);

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void getQueueSet_WithNoReadableQueueData_ShouldReturnEmptySet() {
        String topic = "test-topic";
        RouteInfo route = mock(RouteInfo.class);
        when(route.isQueueMappingEmpty()).thenReturn(true);

        List<QueueInfo> queueDatas = new ArrayList<>();
        QueueInfo qi = mock(QueueInfo.class);
        when(qi.getPerm()).thenReturn(0);
        when(qi.getReadQueueNums()).thenReturn(4);
        queueDatas.add(qi);
        when(route.getQueueDatas()).thenReturn(queueDatas);

        Set<MessageQueue> result = RouteConverter.getQueueSet(topic, route);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getQueueSet_WithEmptyQueueData_ShouldReturnEmptySet() {
        String topic = "test-topic";
        RouteInfo route = mock(RouteInfo.class);
        when(route.isQueueMappingEmpty()).thenReturn(true);
        when(route.getQueueDatas()).thenReturn(new ArrayList<>());

        Set<MessageQueue> result = RouteConverter.getQueueSet(topic, route);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getQueueMap_WithEmptyMapping_ShouldReturnEmptyMap() {
        String topic = "test-topic";
        RouteInfo route = mock(RouteInfo.class);
        when(route.isQueueMappingEmpty()).thenReturn(true);

        var result = RouteConverter.getQueueMap(topic, route);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}