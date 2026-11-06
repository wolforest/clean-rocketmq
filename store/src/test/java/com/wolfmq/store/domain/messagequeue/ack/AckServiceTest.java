package com.wolfmq.store.domain.messagequeue.ack;

import com.wolfmq.common.config.business.MessageConfig;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.consumer.ack.AckBuffer;
import com.wolfmq.common.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import com.wolfmq.common.domain.consumer.consume.pop.checkpoint.PopCheckPointWrapper;
import com.wolfmq.store.domain.messagequeue.queue.EnqueueService;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AckServiceTest {
    @Test
    void addCheckPointSkipsDuplicate() {
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setEnablePopBufferMerge(false);
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessage(messageConfig);

        AckBuffer ackBuffer = new AckBuffer(messageConfig);
        EnqueueService enqueueService = mock(EnqueueService.class);
        AckOffset offsetService = mock(AckOffset.class);

        AckService service = new AckService(storeConfig, "REVIVE_TOPIC", ackBuffer, enqueueService, offsetService);

        PopCheckPoint point = PopCheckPoint.builder()
            .topic("TOPIC_A")
            .cid("GROUP_A")
            .queueId(0)
            .startOffset(1)
            .popTime(100)
            .invisibleTime(1000)
            .brokerName("BROKER")
            .num((byte) 1)
            .build();

        PopCheckPointWrapper wrapper = new PopCheckPointWrapper(1, -1, point, 2);
        ackBuffer.enqueue(wrapper);

        service.addCheckPoint(point, 1, -1, 2);

        verifyNoInteractions(enqueueService);
    }

}
