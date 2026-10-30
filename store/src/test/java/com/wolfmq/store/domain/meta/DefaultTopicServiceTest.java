package com.wolfmq.store.domain.meta;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.core.constant.MQConstants;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.topic.KeyBuilder;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicValidator;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.domain.server.store.StoreState;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.server.bootstrap.StoreRegister;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTopicServiceTest {
    private final StoreState storeState = new StoreState();

    @TempDir
    Path tempDir;

    @Test
    void saveTopic_ShouldPersistAndRegisterTopic() {
        storeState.setStateMachineVersion(11L);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        String storePath = tempDir.resolve("topic.json").toString();

        DefaultTopicService service = new DefaultTopicService(storeState, storeConfig, storePath, offsetService);
        service.inject(consumeQueueManager, storeRegister);

        Topic topic = new Topic();
        topic.setTopicName("topic-save");
        service.saveTopic(topic);

        assertTrue(service.exists("topic-save"));
        verify(storeRegister).registerTopic(topic);
    }

    @Test
    void deleteTopic_ShouldDeleteMainAndRetryTopics() {
        storeState.setStateMachineVersion(22L);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        String storePath = tempDir.resolve("topic.json").toString();

        DefaultTopicService service = new DefaultTopicService(storeState, storeConfig, storePath, offsetService);
        service.inject(consumeQueueManager, storeRegister);

        Topic mainTopic = new Topic();
        mainTopic.setTopicName("topic-main");
        Topic retryTopic = new Topic();
        retryTopic.setTopicName(KeyBuilder.buildPopRetryTopic("topic-main", "group-a"));

        service.putTopic(mainTopic);
        service.putTopic(retryTopic);
        when(offsetService.findGroupByTopic("topic-main")).thenReturn(Set.of("group-a"));

        service.deleteTopic("topic-main");

        assertFalse(service.exists("topic-main"));
        assertFalse(service.exists(retryTopic.getTopicName()));
        verify(offsetService).deleteByTopic("topic-main");
        verify(offsetService).deleteByTopic(retryTopic.getTopicName());
        verify(consumeQueueManager).deleteByTopic("topic-main");
        verify(consumeQueueManager).deleteByTopic(retryTopic.getTopicName());
    }

    @Test
    void load_WhenFileMissing_ShouldRegisterSystemTopics() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        DefaultTopicService service = new DefaultTopicService(
            storeState,
            storeConfig,
            tempDir.resolve("topic.json").toString(),
            offsetService
        );
        service.inject(mock(ConsumeQueueManager.class), mock(StoreRegister.class));

        service.load();

        assertTrue(service.exists(TopicValidator.RMQ_SYS_SELF_TEST_TOPIC));
        assertTrue(service.exists(TopicValidator.RMQ_SYS_SCHEDULE_TOPIC));
        assertTrue(service.exists(storeConfig.getCluster() + "_" + MQConstants.REPLY_TOPIC_POSTFIX));
    }
}
