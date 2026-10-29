package com.wolfmq.store.domain.meta;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import com.wolfmq.store.server.bootstrap.StoreRegister;
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
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
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
