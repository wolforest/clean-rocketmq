package com.wolfmq.store.domain.meta;

import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;

import com.wolfmq.common.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.common.server.store.api.meta.SubscriptionStore;
import com.wolfmq.common.server.store.StoreState;
import com.wolfmq.common.server.store.api.meta.TopicStore;
import com.wolfmq.common.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.common.domain.meta.service.ConsumeOrderService;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreRegister;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterMetaBeans(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());

        context.register(storeConfig);
        context.register(new StoreState(), StoreState.class);
        StoreCheckpoint __cp = new StoreCheckpoint(anotherDir.toString()); context.register(__cp, CheckPoint.class);

        ConsumeQueueFacade consumeQueueFacade = mock(ConsumeQueueFacade.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        context.register(consumeQueueFacade, ConsumeQueueFacade.class);
        context.register(storeRegister, StoreRegister.class);

        MetaBootstrap bootstrap = new MetaBootstrap(context);
        bootstrap.initialize();

        ConsumeOffsetService offsetService = context.getBean(ConsumeOffsetService.class);
        assertNotNull(offsetService);

        ConsumeOrderService orderService = context.getBean(ConsumeOrderService.class);
        assertNotNull(orderService);

        TopicService topicService = context.getBean(TopicService.class);
        assertNotNull(topicService);

        ConsumeOffsetStore offsetStore = context.getBean(ConsumeOffsetStore.class, ContextNamespace.API);
        assertNotNull(offsetStore);

        SubscriptionStore subscriptionStore = context.getBean(SubscriptionStore.class, ContextNamespace.API);
        assertNotNull(subscriptionStore);

        TopicStore topicStore = context.getBean(TopicStore.class, ContextNamespace.API);
        assertNotNull(topicStore);
    }
}
