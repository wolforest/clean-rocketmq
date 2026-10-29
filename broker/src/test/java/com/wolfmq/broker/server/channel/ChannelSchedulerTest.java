package com.wolfmq.broker.server.channel;

import com.wolfmq.broker.api.ConsumerController;
import com.wolfmq.broker.api.ProducerController;
import com.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class ChannelSchedulerTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private ProducerController producerController;

    @Mock
    private ConsumerController consumerController;

    private ChannelScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new ChannelScheduler(brokerConfig);

        scheduler.inject(producerController, consumerController);
    }

    @Test
    void testConstructor() {
        assertNotNull(scheduler);
    }

    @Test
    void testGetState_initial() {
        assertNotNull(scheduler.getState());
    }
}
