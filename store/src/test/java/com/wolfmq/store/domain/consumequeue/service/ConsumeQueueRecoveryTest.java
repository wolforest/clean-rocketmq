package com.wolfmq.store.domain.consumequeue.service;

import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.consumequeue.ConsumeQueue;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeQueueRecoveryTest {

    private ConsumeQueueConfig config;
    private CheckPoint checkPoint;
    private OffsetCodec offsetCodec;
    private ConsumeQueueRecovery recovery;

    @BeforeEach
    void setUp() {
        config = new ConsumeQueueConfig();
        checkPoint = mock(CheckPoint.class);
        offsetCodec = mock(OffsetCodec.class);
        recovery = new ConsumeQueueRecovery(config, checkPoint, offsetCodec);
    }

    @Test
    void testRegisterAddsQueue() {
        ConsumeQueue queue = mock(ConsumeQueue.class);
        assertDoesNotThrow(() -> recovery.register(queue));
    }

    @Test
    void testRecoverWithEmptyQueueSet() {
        assertDoesNotThrow(() -> recovery.recover());
    }

    @Test
    void testRegisterMultipleQueues() {
        ConsumeQueue queue1 = mock(ConsumeQueue.class);
        ConsumeQueue queue2 = mock(ConsumeQueue.class);
        assertDoesNotThrow(() -> recovery.register(queue1));
        assertDoesNotThrow(() -> recovery.register(queue2));
    }
}
