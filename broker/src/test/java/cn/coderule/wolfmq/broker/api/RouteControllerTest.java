package cn.coderule.wolfmq.broker.api;

import cn.coderule.wolfmq.broker.domain.meta.BrokerTopicService;
import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.broker.domain.meta.SubscriptionService;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteControllerTest {

    private RouteController controller;
    private RouteService routeService;
    private BrokerTopicService topicService;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        routeService = mock(RouteService.class);
        topicService = mock(BrokerTopicService.class);
        subscriptionService = mock(SubscriptionService.class);
        controller = new RouteController(routeService, topicService, subscriptionService);
    }

    @Test
    void constructor_ShouldCreateController() {
        assertNotNull(controller);
    }

    @Test
    void getTopic_ShouldDelegateToTopicService() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        when(topicService.getTopic("test-topic")).thenReturn(topic);

        Topic result = controller.getTopic("test-topic");
        assertNotNull(result);
        assertEquals("test-topic", result.getTopicName());
    }
}
