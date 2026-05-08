package cn.coderule.wolfmq.store.domain.meta.it;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.meta.order.OrderRequest;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.SubscriptionService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.meta.DefaultConsumeOffsetService;
import cn.coderule.wolfmq.store.domain.meta.DefaultConsumeOrderService;
import cn.coderule.wolfmq.store.domain.meta.DefaultSubscriptionService;
import cn.coderule.wolfmq.store.domain.meta.DefaultTopicService;
import cn.coderule.wolfmq.store.domain.meta.OrderLockCleaner;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import cn.coderule.wolfmq.store.server.bootstrap.StoreRegister;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaIntegrationTest {

    @TempDir
    Path tmpDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {
        storeConfig = ConfigMock.createStoreConfig(tmpDir.toString());
        StoreContext.setStateMachineVersion(1L);
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
    }

    // --- DefaultConsumeOffsetService ---

    @Test
    void testConsumeOffsetLoadStoreAndReload() {
        String storePath = tmpDir.resolve("offset.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();

        service.putOffset("groupA", "topicA", 0, 100L);
        service.putOffset("groupA", "topicA", 1, 200L);
        service.putOffset("groupB", "topicB", 0, 300L);
        service.store();

        DefaultConsumeOffsetService reloaded = new DefaultConsumeOffsetService(storeConfig, storePath);
        reloaded.load();

        assertEquals(100L, reloaded.getOffset("groupA", "topicA", 0));
        assertEquals(200L, reloaded.getOffset("groupA", "topicA", 1));
        assertEquals(300L, reloaded.getOffset("groupB", "topicB", 0));
        assertEquals(-1L, reloaded.getOffset("groupC", "topicC", 0), "non-existent offset should be -1");
    }

    @Test
    void testConsumeOffsetDeleteByTopic() {
        String storePath = tmpDir.resolve("offset-del-topic.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();

        service.putOffset("groupA", "topicX", 0, 50L);
        service.putOffset("groupA", "topicX", 1, 60L);
        service.putOffset("groupB", "topicY", 0, 70L);

        service.deleteByTopic("topicX");

        assertEquals(-1L, service.getOffset("groupA", "topicX", 0));
        assertEquals(-1L, service.getOffset("groupA", "topicX", 1));
        assertEquals(70L, service.getOffset("groupB", "topicY", 0));
    }

    @Test
    void testConsumeOffsetDeleteByGroup() {
        String storePath = tmpDir.resolve("offset-del-group.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();

        service.putOffset("groupA", "topicX", 0, 50L);
        service.putOffset("groupB", "topicX", 0, 60L);

        service.deleteByGroup("groupA");

        assertEquals(-1L, service.getOffset("groupA", "topicX", 0));
        assertEquals(60L, service.getOffset("groupB", "topicX", 0));
    }

    @Test
    void testConsumeOffsetFindTopicByGroupAndGroupByTopic() {
        String storePath = tmpDir.resolve("offset-find.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();

        service.putOffset("g1", "t1", 0, 10L);
        service.putOffset("g1", "t2", 0, 20L);
        service.putOffset("g2", "t1", 0, 30L);

        Set<String> topics = service.findTopicByGroup("g1");
        assertTrue(topics.contains("t1"));
        assertTrue(topics.contains("t2"));

        Set<String> groups = service.findGroupByTopic("t1");
        assertTrue(groups.contains("g1"));
        assertTrue(groups.contains("g2"));
    }

    @Test
    void testConsumeOffsetGetAndRemove() {
        String storePath = tmpDir.resolve("offset-remove.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();

        service.putOffset("g1", "t1", 0, 100L);
        service.putOffset("g1", "t1", 0, 200L);

        Long removed = service.getAndRemove("g1", "t1", 0);
        assertEquals(200L, removed.longValue());

        Long removedAgain = service.getAndRemove("g1", "t1", 0);
        assertEquals(-1L, removedAgain.longValue(), "getAndRemove on non-existent key should return -1");
    }

    @Test
    void testConsumeOffsetContainsResetOffset() {
        String storePath = tmpDir.resolve("offset-reset.json").toString();
        DefaultConsumeOffsetService service = new DefaultConsumeOffsetService(storeConfig, storePath);
        service.load();

        assertFalse(service.containsResetOffset("g1", "t1", 0));
    }

    // --- DefaultConsumeOrderService ---

    @Test
    void testConsumeOrderLockUnlockAndCommit() {
        String storePath = tmpDir.resolve("order.json").toString();
        OrderLockCleaner cleaner = mock(OrderLockCleaner.class);
        DefaultConsumeOrderService service = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        service.load();

        long now = System.currentTimeMillis();
        OrderRequest lockRequest = OrderRequest.builder()
            .topicName("order-topic")
            .consumerGroup("order-group")
            .queueId(0)
            .attemptId("attempt-1")
            .dequeueTime(now)
            .invisibleTime(60_000L)
            .queueOffset(10L)
            .offsetList(List.of(10L))
            .build();

        service.lock(lockRequest);

        OrderRequest checkRequest = OrderRequest.builder()
            .topicName("order-topic")
            .consumerGroup("order-group")
            .queueId(0)
            .attemptId("attempt-2")
            .dequeueTime(now)
            .invisibleTime(60_000L)
            .queueOffset(10L)
            .offsetList(List.of(10L))
            .build();
        assertTrue(service.isLocked(checkRequest));

        long committedOffset = service.commit(lockRequest);
        assertEquals(11L, committedOffset);

        service.unlock(lockRequest);
        assertFalse(service.isLocked(checkRequest));
    }

    @Test
void testConsumeOrderStoreAndReload() {
        String storePath = tmpDir.resolve("order-persist.json").toString();
        OrderLockCleaner cleaner = mock(OrderLockCleaner.class);

        DefaultConsumeOrderService service1 = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        service1.load();

        long now = System.currentTimeMillis();
        OrderRequest lockRequest = OrderRequest.builder()
            .topicName("persist-topic")
            .consumerGroup("persist-group")
            .queueId(1)
            .attemptId("attempt-1")
            .dequeueTime(now)
            .invisibleTime(30000L)
            .queueOffset(50L)
            .offsetList(List.of(50L))
            .build();

        service1.lock(lockRequest);
        service1.store();

        DefaultConsumeOrderService service2 = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        assertDoesNotThrow(service2::load, "load should not throw after store");

        service2.lock(lockRequest);
        OrderRequest checkRequest = OrderRequest.builder()
            .topicName("persist-topic")
            .consumerGroup("persist-group")
            .queueId(1)
            .attemptId("attempt-check")
            .dequeueTime(now)
            .invisibleTime(30000L)
            .queueOffset(50L)
            .offsetList(List.of(50L))
            .build();
        assertTrue(service2.isLocked(checkRequest), "re-locked order should be visible");

        service2.unlock(lockRequest);
        assertFalse(service2.isLocked(checkRequest), "order should be unlocked after unlock call");
    }

    @Test
    void testConsumeOrderUpdateInvisible() {
        String storePath = tmpDir.resolve("order-invisible.json").toString();
        OrderLockCleaner cleaner = mock(OrderLockCleaner.class);
        DefaultConsumeOrderService service = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        service.load();

        long now = System.currentTimeMillis();
        OrderRequest request = OrderRequest.builder()
            .topicName("inv-topic")
            .consumerGroup("inv-group")
            .queueId(0)
            .attemptId("attempt-1")
            .dequeueTime(now)
            .invisibleTime(30_000L)
            .queueOffset(100L)
            .offsetList(List.of(100L))
            .build();

        service.lock(request);
        assertDoesNotThrow(() -> service.updateInvisible(request));
        service.unlock(request);
    }

    // --- DefaultTopicService ---

    @Test
    void testTopicServiceLoadRegistersSystemTopics() {
        String storePath = tmpDir.resolve("topic-sys.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager cqManager =
            mock(cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager.class);

        DefaultTopicService service = new DefaultTopicService(storeConfig, storePath, offsetService);
        service.inject(cqManager, storeRegister);

        assertDoesNotThrow(() -> service.load());

        assertTrue(service.exists(cn.coderule.wolfmq.domain.domain.meta.topic.TopicValidator.RMQ_SYS_SELF_TEST_TOPIC), "SELF_TEST_TOPIC should exist after load");
        assertTrue(service.exists(cn.coderule.wolfmq.domain.domain.meta.topic.TopicValidator.RMQ_SYS_SCHEDULE_TOPIC), "SCHEDULE topic should exist after load");
    }

    @Test
    void testTopicServicePutAndExists() {
        String storePath = tmpDir.resolve("topic-put.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager cqManager =
            mock(cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager.class);

        DefaultTopicService service = new DefaultTopicService(storeConfig, storePath, offsetService);
        service.inject(cqManager, storeRegister);
        service.load();

        Topic topic = new Topic();
        topic.setTopicName("MY_TOPIC");
        service.putTopic(topic);

        assertTrue(service.exists("MY_TOPIC"));
        assertFalse(service.exists("NON_EXISTENT_TOPIC"));
    }

    @Test
    void testTopicServiceSaveAndReload() {
        String storePath = tmpDir.resolve("topic-save.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager cqManager =
            mock(cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager.class);

        DefaultTopicService service1 = new DefaultTopicService(storeConfig, storePath, offsetService);
        service1.inject(cqManager, storeRegister);
        service1.load();

        Topic topic = new Topic();
        topic.setTopicName("PERSISTENT_TOPIC");
        service1.saveTopic(topic);
        service1.store();

        DefaultTopicService service2 = new DefaultTopicService(storeConfig, storePath, offsetService);
        service2.inject(cqManager, storeRegister);
        service2.load();

        assertTrue(service2.exists("PERSISTENT_TOPIC"));
    }

    @Test
    void testTopicServiceGetTopic() {
        String storePath = tmpDir.resolve("topic-get.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager cqManager =
            mock(cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager.class);

        DefaultTopicService service = new DefaultTopicService(storeConfig, storePath, offsetService);
        service.inject(cqManager, storeRegister);
        service.load();

        Topic topic = new Topic();
        topic.setTopicName("GET_TOPIC");
        service.putTopic(topic);

        Topic retrieved = service.getTopic("GET_TOPIC");
        assertNotNull(retrieved);
        assertEquals("GET_TOPIC", retrieved.getTopicName());

        assertNull(service.getTopic("NON_EXISTENT"));
    }

    // --- DefaultSubscriptionService ---

    @Test
    void testSubscriptionServicePutAndExists() {
        String storePath = tmpDir.resolve("subscription.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);

        DefaultSubscriptionService service = new DefaultSubscriptionService(storePath, offsetService);
        service.load();

        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName("MY_GROUP");
        service.putGroup(group);

        assertTrue(service.existsGroup("MY_GROUP"));
        assertFalse(service.existsGroup("NON_EXISTENT"));
    }

    @Test
    void testSubscriptionServiceSaveAndReload() {
        String storePath = tmpDir.resolve("sub-save.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);

        DefaultSubscriptionService service1 = new DefaultSubscriptionService(storePath, offsetService);
        service1.load();

        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName("PERSISTENT_GROUP");
        service1.saveGroup(group);
        service1.store();

        DefaultSubscriptionService service2 = new DefaultSubscriptionService(storePath, offsetService);
        service2.load();

        assertTrue(service2.existsGroup("PERSISTENT_GROUP"));
    }

    @Test
    void testSubscriptionServiceDeleteGroup() {
        String storePath = tmpDir.resolve("sub-delete.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);

        DefaultSubscriptionService service = new DefaultSubscriptionService(storePath, offsetService);
        service.load();

        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName("DELETE_GROUP");
        service.putGroup(group);
        assertTrue(service.existsGroup("DELETE_GROUP"));

        service.deleteGroup("DELETE_GROUP", false);
        assertFalse(service.existsGroup("DELETE_GROUP"));
    }

    @Test
    void testSubscriptionServiceDeleteGroupWithOffsetCleanup() {
        String storePath = tmpDir.resolve("sub-delete-offset.json").toString();
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);

        DefaultSubscriptionService service = new DefaultSubscriptionService(storePath, offsetService);
        service.load();

        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName("OFFSET_GROUP");
        service.putGroup(group);

        service.deleteGroup("OFFSET_GROUP", true);
        verify(offsetService).deleteByGroup("OFFSET_GROUP");
    }

    // --- Cross-module: TopicService + ConsumeOffsetService ---

    @Test
    void testTopicDeleteCleansOffsets() {
        String offsetPath = tmpDir.resolve("cross-offset.json").toString();
        String topicPath = tmpDir.resolve("cross-topic.json").toString();

        DefaultConsumeOffsetService offsetService = new DefaultConsumeOffsetService(storeConfig, offsetPath);
        offsetService.load();

        StoreRegister storeRegister = mock(StoreRegister.class);
        cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager cqManager =
            mock(cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager.class);

        DefaultTopicService topicService = new DefaultTopicService(storeConfig, topicPath, offsetService);
        topicService.inject(cqManager, storeRegister);
        topicService.load();

        offsetService.putOffset("g1", "cross-topic", 0, 100L);
        offsetService.putOffset("g2", "cross-topic", 0, 200L);

        Topic topic = new Topic();
        topic.setTopicName("cross-topic");
        topicService.putTopic(topic);

        topicService.deleteTopic("cross-topic");

        assertFalse(topicService.exists("cross-topic"));
        verify(cqManager).deleteByTopic("cross-topic");
    }
}