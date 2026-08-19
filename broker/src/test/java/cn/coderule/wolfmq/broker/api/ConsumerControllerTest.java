package cn.coderule.wolfmq.broker.api;

import cn.coderule.wolfmq.broker.domain.consumer.Consumer;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ConsumerInfo;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerControllerTest {

    private ConsumerController controller;
    private Consumer consumer;
    private BrokerConfig brokerConfig;

    @BeforeEach
    void setUp() {
        brokerConfig = ConfigMock.createBrokerConfig();
        consumer = mock(Consumer.class);
        controller = new ConsumerController(brokerConfig, consumer);
    }

    @Test
    void constructor_ShouldCreateController() {
        assertNotNull(controller);
    }

    @Test
    void register_ShouldDelegateToConsumer() {
        ConsumerInfo info = mock(ConsumerInfo.class);
        when(info.getGroupName()).thenReturn("test-group");
        when(consumer.register(info)).thenReturn(true);

        boolean result = controller.register(info);
        assertTrue(result);
        verify(consumer).register(info);
    }

    @Test
    void unregister_ShouldDelegateToConsumer() {
        ConsumerInfo info = mock(ConsumerInfo.class);
        when(info.getGroupName()).thenReturn("test-group");
        controller.unregister(info);
        verify(consumer).unregister(info);
    }

    @Test
    void scanIdleChannels_ShouldDelegateToConsumer() {
        controller.scanIdleChannels();
        verify(consumer).scanIdleChannels();
    }
}
