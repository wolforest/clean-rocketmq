package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.store.domain.consumequeue.ConsumeQueueManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StoreRegister;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTopicServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveTopic_ShouldPersistAndRegisterTopic() {
        StoreContext.setStateMachineVersion(11L);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        String storePath = tempDir.resolve("topic.json").toString();

        DefaultTopicService service = new DefaultTopicService(storeConfig, storePath, offsetService);
        service.inject(consumeQueueManager, storeRegister);

        Topic topic = new Topic();
        topic.setTopicName("topic-save");
        service.saveTopic(topic);

        assertTrue(service.exists("topic-save"));
        verify(storeRegister).registerTopic(topic);
    }

    @Test
    void deleteTopic_ShouldDeleteMainAndRetryTopics() {
        StoreContext.setStateMachineVersion(22L);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        String storePath = tempDir.resolve("topic.json").toString();

        DefaultTopicService service = new DefaultTopicService(storeConfig, storePath, offsetService);
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

