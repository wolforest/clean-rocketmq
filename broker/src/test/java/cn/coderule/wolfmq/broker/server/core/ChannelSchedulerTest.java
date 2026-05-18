package cn.coderule.wolfmq.broker.server.core;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChannelSchedulerTest {

    @Mock
    private BrokerConfig brokerConfig;

    private ChannelScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new ChannelScheduler(brokerConfig);
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