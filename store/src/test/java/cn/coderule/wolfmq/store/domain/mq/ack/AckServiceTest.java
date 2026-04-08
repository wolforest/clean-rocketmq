package cn.coderule.wolfmq.store.domain.mq.ack;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckBuffer;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPointWrapper;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
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
        storeConfig.setMessageConfig(messageConfig);

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
