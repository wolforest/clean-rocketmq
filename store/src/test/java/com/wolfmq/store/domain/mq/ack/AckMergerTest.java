package com.wolfmq.store.domain.mq.ack;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.domain.consumer.ack.AckBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AckMergerTest {

    @Test
    void getServiceNameUsesSimpleName() {
        MessageConfig messageConfig = new MessageConfig();
        AckBuffer ackBuffer = new AckBuffer(messageConfig);
        AckMerger merger = new AckMerger(messageConfig, "REVIVE_TOPIC", ackBuffer);

        assertEquals("AckMerger", merger.getServiceName());
    }

    @Test
    void runReturnsWhenBufferMergeDisabled() {
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setEnablePopBufferMerge(false);
        AckBuffer ackBuffer = new AckBuffer(messageConfig);
        AckMerger merger = new AckMerger(messageConfig, "REVIVE_TOPIC", ackBuffer);

        merger.run();
    }
}
