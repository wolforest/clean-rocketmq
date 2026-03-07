package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.store.StorePath;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConsumeQueueFactory
 */
class ConsumeQueueFactoryTest {

    private ConsumeQueueFactory factory;
    private MockTopicService topicService;

    @TempDir
    Path tmpDir;

    @BeforeEach
    void setUp() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tmpDir.toString());
        ConsumeQueueConfig config = storeConfig.getConsumeQueueConfig();
        this.topicService = new MockTopicService();
        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());

        factory = new ConsumeQueueFactory(config, topicService, checkpoint);
    }

    @Test
    void testConstructor() {
        assertNotNull(factory);
        assertNotNull(getTopicMap(factory));
        assertTrue(getTopicMap(factory).isEmpty());
    }

    @Test
    void testCreateAll_Empty() {
        assertDoesNotThrow(() -> factory.createAll());
    }

    @Test
    void testGetOrCreate_TopicNotExists() {
        ConsumeQueue queue = factory.getOrCreate("NON_EXISTENT", 0);

        assertNotNull(queue);
        assertInstanceOf(ErrorConsumeQueue.class, queue);
    }

    @Test
    void testGetOrCreate_CreateNewQueue() {
        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);

        ConsumeQueue queue = factory.getOrCreate(topic, 0);

        assertNotNull(queue);
        assertEquals(topic, queue.getTopic());
        assertEquals(0, queue.getQueueId());
    }

    @Test
    void testGetOrCreate_ReturnExistingQueue() {
        String topic = "TEST_TOPIC";
        int queueId = 5;
        topicService.addTopic(topic);

        ConsumeQueue queue1 = factory.getOrCreate(topic, queueId);
        ConsumeQueue queue2 = factory.getOrCreate(topic, queueId);

        assertSame(queue1, queue2);
    }

    @Test
    void testGetOrCreate_MultipleQueues() {
        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);

        ConsumeQueue queue0 = factory.getOrCreate(topic, 0);
        ConsumeQueue queue1 = factory.getOrCreate(topic, 1);
        ConsumeQueue queue2 = factory.getOrCreate(topic, 2);

        assertNotSame(queue0, queue1);
        assertNotSame(queue1, queue2);
        assertNotSame(queue0, queue2);

        assertEquals(0, queue0.getQueueId());
        assertEquals(1, queue1.getQueueId());
        assertEquals(2, queue2.getQueueId());
    }

    @Test
    void testGetOrCreate_MultipleTopics() {
        topicService.addTopic("TOPIC_A");
        topicService.addTopic("TOPIC_B");

        ConsumeQueue queueA = factory.getOrCreate("TOPIC_A", 0);
        ConsumeQueue queueB = factory.getOrCreate("TOPIC_B", 0);

        assertNotSame(queueA, queueB);
        assertEquals("TOPIC_A", queueA.getTopic());
        assertEquals("TOPIC_B", queueB.getTopic());
    }

    @Test
    void testGet_TopicNotExists() {
        ConsumeQueue queue = factory.get("NON_EXISTENT", 0);
        assertNull(queue);
    }

    @Test
    void testGet_QueueNotExists() {
        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);
        factory.getOrCreate(topic, 0);

        ConsumeQueue queue = factory.get(topic, 999);
        assertNull(queue);
    }

    @Test
    void testGet_Exists() {
        String topic = "TEST_TOPIC";
        int queueId = 3;
        topicService.addTopic(topic);

        ConsumeQueue created = factory.getOrCreate(topic, queueId);
        ConsumeQueue retrieved = factory.get(topic, queueId);

        assertSame(created, retrieved);
    }

    @Test
    void testExists_TopicNotExists() {
        assertFalse(factory.exists("NON_EXISTENT", 0));
    }

    @Test
    void testExists_QueueNotExists() {
        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);
        factory.getOrCreate(topic, 0);

        assertFalse(factory.exists(topic, 999));
    }

    @Test
    void testExists_True() {
        String topic = "TEST_TOPIC";
        int queueId = 2;
        topicService.addTopic(topic);

        factory.getOrCreate(topic, queueId);

        assertTrue(factory.exists(topic, queueId));
    }

    @Test
    void testAddCreateHook() {
        MockRegistry hook = new MockRegistry();

        factory.addCreateHook(hook);

        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);
        factory.getOrCreate(topic, 0);

        assertEquals(1, hook.registerCount);
    }

    @Test
    void testAddMultipleCreateHooks() {
        MockRegistry hook1 = new MockRegistry();
        MockRegistry hook2 = new MockRegistry();

        factory.addCreateHook(hook1);
        factory.addCreateHook(hook2);

        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);
        factory.getOrCreate(topic, 0);

        assertEquals(1, hook1.registerCount);
        assertEquals(1, hook2.registerCount);
    }

    @Test
    void testRegister_NoHooks() {
        String topic = "TEST_TOPIC";
        topicService.addTopic(topic);

        ConsumeQueue queue = factory.getOrCreate(topic, 0);
        assertNotNull(queue);
    }

    @Test
    void testTopicMapStructure() {
        String topic1 = "TOPIC_1";
        String topic2 = "TOPIC_2";
        topicService.addTopic(topic1);
        topicService.addTopic(topic2);

        factory.getOrCreate(topic1, 0);
        factory.getOrCreate(topic1, 1);
        factory.getOrCreate(topic2, 0);

        ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueue>> topicMap
            = getTopicMap(factory);

        assertEquals(2, topicMap.size());
        assertEquals(2, topicMap.get(topic1).size());
        assertEquals(1, topicMap.get(topic2).size());
    }

    @Test
    void testGetOrCreate_DifferentTopicsAndQueues() {
        topicService.addTopic("TOPIC_A");
        topicService.addTopic("TOPIC_B");

        ConsumeQueue queueA0 = factory.getOrCreate("TOPIC_A", 0);
        ConsumeQueue queueA1 = factory.getOrCreate("TOPIC_A", 1);
        ConsumeQueue queueB0 = factory.getOrCreate("TOPIC_B", 0);
        ConsumeQueue queueB1 = factory.getOrCreate("TOPIC_B", 1);

        assertNotSame(queueA0, queueA1);
        assertNotSame(queueA0, queueB0);
        assertNotSame(queueA0, queueB1);
        assertNotSame(queueA1, queueB0);
        assertNotSame(queueA1, queueB1);
        assertNotSame(queueB0, queueB1);
    }

    // Helper method to access protected topicMap field
    @SuppressWarnings("unchecked")
    private ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueue>> getTopicMap(ConsumeQueueFactory factory) {
        try {
            java.lang.reflect.Field field = ConsumeQueueFactory.class.getDeclaredField("topicMap");
            field.setAccessible(true);
            return (ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueue>>) field.get(factory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Mock implementation of TopicService
    private static class MockTopicService implements TopicService {
        private final Set<String> topics = ConcurrentHashMap.newKeySet();

        void addTopic(String topic) {
            topics.add(topic);
        }

        @Override
        public boolean exists(String topicName) {
            return topics.contains(topicName);
        }

        @Override
        public void load() {
        }

        @Override
        public void store() {
        }

        @Override
        public cn.coderule.minimq.domain.domain.meta.topic.Topic getTopic(String topicName) {
            return null;
        }

        @Override
        public void saveTopic(cn.coderule.minimq.domain.domain.meta.topic.Topic topic) {
        }

        @Override
        public void putTopic(cn.coderule.minimq.domain.domain.meta.topic.Topic topic) {
        }

        @Override
        public void deleteTopic(String topicName) {
        }

        @Override
        public cn.coderule.minimq.domain.domain.meta.topic.TopicMap getTopicMap() {
            return null;
        }

        @Override
        public void updateOrderConfig(java.util.Map<String, String> orderMap) {
        }
    }

    // Mock implementation of ConsumeQueueRegistry
    private static class MockRegistry implements ConsumeQueueRegistry {
        int registerCount = 0;

        @Override
        public void register(ConsumeQueue queue) {
            registerCount++;
        }
    }
}
