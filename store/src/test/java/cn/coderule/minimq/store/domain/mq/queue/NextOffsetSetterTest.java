package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.store.domain.consumequeue.ConsumeQueueManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NextOffsetSetterTest {

    @Test
    void setOffsetWhenQueueNotExistsUsesOldOffsetOnSlave() throws Exception {
        Path tempDir = Files.createTempDirectory("mq-next-offset");
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.setGroupNo(1);
        storeConfig.getMetaConfig().setEnableOffsetCheckInSlave(false);

        ConsumeQueueManager consumeQueue = mock(ConsumeQueueManager.class);
        when(consumeQueue.existsQueue("TOPIC_A", 0)).thenReturn(false);

        NextOffsetSetter setter = new NextOffsetSetter(storeConfig, consumeQueue);

        DequeueRequest request = DequeueRequest.builder()
            .topicName("TOPIC_A")
            .consumerGroup("GROUP_A")
            .queueId(0)
            .offset(7)
            .build();

        DequeueResult result = new DequeueResult();
        setter.set(request, result);

        assertEquals(MessageStatus.NO_MATCHED_LOGIC_QUEUE, result.getStatus());
        assertEquals(0, result.getMinOffset());
        assertEquals(0, result.getMaxOffset());
        assertEquals(7, result.getNextOffset());
    }

    @Test
    void setOffsetByMessageListSetsNextOffsetToMaxPlusOne() throws Exception {
        Path tempDir = Files.createTempDirectory("mq-next-offset2");
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());

        ConsumeQueueManager consumeQueue = mock(ConsumeQueueManager.class);
        when(consumeQueue.existsQueue("TOPIC_B", 1)).thenReturn(true);
        when(consumeQueue.getMinOffset("GROUP_B", 1)).thenReturn(1L);
        when(consumeQueue.getMaxOffset("GROUP_B", 1)).thenReturn(10L);

        NextOffsetSetter setter = new NextOffsetSetter(storeConfig, consumeQueue);

        DequeueRequest request = DequeueRequest.builder()
            .topicName("TOPIC_B")
            .consumerGroup("GROUP_B")
            .queueId(1)
            .offset(3)
            .build();

        MessageBO first = MessageMock.createMessage("TOPIC_B", 1, 3);
        MessageBO second = MessageMock.createMessage("TOPIC_B", 1, 8);
        DequeueResult result = DequeueResult.success(List.of(first, second));

        setter.set(request, result);

        assertEquals(1, result.getMinOffset());
        assertEquals(10, result.getMaxOffset());
        assertEquals(9, result.getNextOffset());
    }
}
