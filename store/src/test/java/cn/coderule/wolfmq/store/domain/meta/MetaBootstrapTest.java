package cn.coderule.wolfmq.store.domain.meta;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.api.meta.ConsumeOffsetStore;
import cn.coderule.wolfmq.domain.domain.store.api.meta.SubscriptionStore;
import cn.coderule.wolfmq.domain.domain.store.api.meta.TopicStore;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import cn.coderule.wolfmq.store.server.bootstrap.StoreRegister;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaBootstrapTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterMetaBeans(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        StoreContext.register(storeConfig);
        StoreContext.CHECK_POINT = new StoreCheckpoint(anotherDir.toString());

        ConsumeQueueManager consumeQueueManager = mock(ConsumeQueueManager.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        StoreContext.register(consumeQueueManager, ConsumeQueueManager.class);
        StoreContext.register(storeRegister, StoreRegister.class);

        MetaBootstrap bootstrap = new MetaBootstrap();
        bootstrap.initialize();

        ConsumeOffsetService offsetService = StoreContext.getBean(ConsumeOffsetService.class);
        assertNotNull(offsetService);

        ConsumeOrderService orderService = StoreContext.getBean(ConsumeOrderService.class);
        assertNotNull(orderService);

        TopicService topicService = StoreContext.getBean(TopicService.class);
        assertNotNull(topicService);

        ConsumeOffsetStore offsetStore = StoreContext.getAPI(ConsumeOffsetStore.class);
        assertNotNull(offsetStore);

        SubscriptionStore subscriptionStore = StoreContext.getAPI(SubscriptionStore.class);
        assertNotNull(subscriptionStore);

        TopicStore topicStore = StoreContext.getAPI(TopicStore.class);
        assertNotNull(topicStore);
    }
}