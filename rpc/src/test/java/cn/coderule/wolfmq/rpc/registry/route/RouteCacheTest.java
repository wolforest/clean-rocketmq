package cn.coderule.wolfmq.rpc.registry.route;

import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.cluster.route.PublishInfo;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouteCacheTest {

    private RouteCache routeCache;

    @BeforeEach
    void setUp() {
        routeCache = new RouteCache();
    }

    @Test
    void testConstructor() {
        assertNotNull(routeCache);
        assertNotNull(routeCache.getRouteMap());
        assertNotNull(routeCache.getSubscriptionMap());
        assertNotNull(routeCache.getAddressMap());
        assertNotNull(routeCache.getPublishMap());
    }

    @Test
    void testTryLock() {
        boolean locked = routeCache.tryLock();
        assertTrue(locked);
        
        routeCache.unlock();
    }

    @Test
    void testContainsRoute() {
        String topic = "test-topic";
        assertFalse(routeCache.containsRoute(topic));
        
        RouteInfo routeInfo = mock(RouteInfo.class);
        routeCache.getRouteMap().put(topic, routeInfo);
        
        assertTrue(routeCache.containsRoute(topic));
    }

    @Test
    void testContainsSubscription() {
        String topic = "test-topic";
        assertFalse(routeCache.containsSubscription(topic));
        
        Set<MessageQueue> queueSet = Set.of(new MessageQueue(topic, "broker-a", 0));
        routeCache.getSubscriptionMap().put(topic, queueSet);
        
        assertTrue(routeCache.containsSubscription(topic));
    }

    @Test
    void testGetRoute() {
        String topic = "test-topic";
        RouteInfo routeInfo = mock(RouteInfo.class);
        routeCache.getRouteMap().put(topic, routeInfo);
        
        RouteInfo result = routeCache.getRoute(topic);
        
        assertSame(routeInfo, result);
    }

    @Test
    void testGetRouteNotFound() {
        RouteInfo result = routeCache.getRoute("non-existent");
        assertNull(result);
    }

    @Test
    void testGetPublishInfo() {
        String topic = "test-topic";
        PublishInfo publishInfo = mock(PublishInfo.class);
        routeCache.getPublishMap().put(topic, publishInfo);
        
        PublishInfo result = routeCache.getPublishInfo(topic);
        
        assertSame(publishInfo, result);
    }

    @Test
    void testGetSubscription() {
        String topic = "test-topic";
        Set<MessageQueue> queueSet = Set.of(new MessageQueue(topic, "broker-a", 0));
        routeCache.getSubscriptionMap().put(topic, queueSet);
        
        Set<MessageQueue> result = routeCache.getSubscription(topic);
        
        assertSame(queueSet, result);
    }

    @Test
    void testGetTopicSet() {
        String topic1 = "topic1";
        String topic2 = "topic2";
        
        routeCache.getRouteMap().put(topic1, mock(RouteInfo.class));
        routeCache.getSubscriptionMap().put(topic2, Set.of());
        
        Set<String> topicSet = routeCache.getTopicSet();
        
        assertTrue(topicSet.contains(topic1));
        assertTrue(topicSet.contains(topic2));
    }

    @Test
    void testRemoveSubscription() {
        String topic = "test-topic";
        routeCache.getSubscriptionMap().put(topic, Set.of());
        
        routeCache.removeSubscription(topic);
        
        assertFalse(routeCache.containsSubscription(topic));
    }

    @Test
    void testGetAddress() {
        String groupName = "broker-group";
        long groupNo = 0L;
        String address = "127.0.0.1:10911";
        
        routeCache.getAddressMap().put(groupName, java.util.Map.of(groupNo, address));
        
        String result = routeCache.getAddress(groupName, groupNo);
        
        assertEquals(address, result);
    }

    @Test
    void testGetAddressNotFound() {
        String result = routeCache.getAddress("non-existent", 0L);
        assertNull(result);
    }

    @Test
    void testGetFirstAddress() {
        String groupName = "broker-group";
        String address = "127.0.0.1:10911";
        
        routeCache.getAddressMap().put(groupName, java.util.Map.of(0L, address));
        
        String result = routeCache.getFirstAddress(groupName);
        
        assertEquals(address, result);
    }

    @Test
    void testGetFirstAddressEmptyMap() {
        String result = routeCache.getFirstAddress("empty-group");
        assertNull(result);
    }
}
