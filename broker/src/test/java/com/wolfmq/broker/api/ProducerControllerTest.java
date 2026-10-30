package com.wolfmq.broker.api;

import com.wolfmq.broker.domain.producer.Producer;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.ClientChannelInfo;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.test.ConfigMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProducerControllerTest {

    private ProducerController controller;
    private Producer producer;
    private BrokerConfig brokerConfig;

    @BeforeEach
    void setUp() {
        brokerConfig = ConfigMock.createBrokerConfig();
        producer = mock(Producer.class);
        controller = new ProducerController(brokerConfig, producer);
    }

    @Test
    void constructor_ShouldCreateController() {
        assertNotNull(controller);
    }

    @Test
    void register_ShouldDelegateToProducer() {
        RequestContext ctx = mock(RequestContext.class);
        ClientChannelInfo info = mock(ClientChannelInfo.class);
        controller.register(ctx, "test-topic", info);
        verify(producer).register(ctx, "test-topic", info);
    }

    @Test
    void unregister_ShouldDelegateToProducer() {
        RequestContext ctx = mock(RequestContext.class);
        ClientChannelInfo info = mock(ClientChannelInfo.class);
        controller.unregister(ctx, "test-group", info);
        verify(producer).unregister(ctx, "test-group", info);
    }

    @Test
    void scanIdleChannels_ShouldDelegateToProducer() {
        controller.scanIdleChannels();
        verify(producer).scanIdleChannels();
    }
}
