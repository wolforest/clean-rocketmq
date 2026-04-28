package cn.coderule.wolfmq.broker.domain.meta;

import cn.coderule.wolfmq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteMockerTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private TopicConfig topicConfig;

    @Mock
    private EmbedTopicStore topicStore;

    private RouteMocker routeMocker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getGroup()).thenReturn("broker-a");
        when(brokerConfig.getCluster()).thenReturn("DefaultCluster");
        when(brokerConfig.getHost()).thenReturn("127.0.0.1");
        when(brokerConfig.getPort()).thenReturn(10911);
        when(brokerConfig.getGroupNo()).thenReturn(0L);
        when(topicConfig.getDefaultQueueNum()).thenReturn(8);
        when(topicConfig.isEnableAutoCreation()).thenReturn(true);
        
        routeMocker = new RouteMocker(brokerConfig, topicConfig, topicStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(routeMocker);
    }

    @Test
    void testGetRouteWithExistingTopic() {
        Topic topic = Topic.builder()
            .topicName("TestTopic")
            .readQueueNums(8)
            .writeQueueNums(8)
            .build();
        
        when(topicStore.getTopic("TestTopic")).thenReturn(topic);
        
        RouteInfo routeInfo = routeMocker.getRoute("TestTopic");
        
        assertNotNull(routeInfo);
        assertEquals("TestTopic", routeInfo.getTopicName());
        assertEquals(1, routeInfo.getQueueDatas().size());
        assertEquals(1, routeInfo.getBrokerDatas().size());
    }

    @Test
    void testGetRouteWithNonExistingTopicAutoCreate() {
        when(topicStore.getTopic("NewTopic")).thenReturn(null);
        when(topicConfig.isEnableAutoCreation()).thenReturn(true);
        
        RouteInfo routeInfo = routeMocker.getRoute("NewTopic");
        
        assertNotNull(routeInfo);
        verify(topicStore).saveTopic(any());
    }

    @Test
    void testGetRouteWithNonExistingTopicNoAutoCreate() {
        when(topicStore.getTopic("NewTopic")).thenReturn(null);
        when(topicConfig.isEnableAutoCreation()).thenReturn(false);
        
        RouteInfo routeInfo = routeMocker.getRoute("NewTopic");
        
        assertNotNull(routeInfo);
        // When auto-creation is disabled, should return empty route
        assertTrue(routeInfo.getQueueDatas().isEmpty());
    }

    @Test
    void testGetRouteNullTopic() {
        when(topicStore.getTopic("NullTopic")).thenReturn(null);
        when(topicConfig.isEnableAutoCreation()).thenReturn(true);
        when(topicConfig.getDefaultQueueNum()).thenReturn(8);
        doThrow(new RuntimeException("Save failed")).when(topicStore).saveTopic(any());
        
        RouteInfo routeInfo = routeMocker.getRoute("NullTopic");
        
        // Should handle exception gracefully
        assertNotNull(routeInfo);
    }
}
