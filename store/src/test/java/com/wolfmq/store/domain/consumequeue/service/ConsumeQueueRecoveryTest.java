package com.wolfmq.store.domain.consumequeue.service;

import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.domain.consumequeue.ConsumeQueue;
import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueRecovery;
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
        recovery = new ConsumeQueueRecovery(config, checkPoint);
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
