package com.wolfmq.store.domain.meta;

import com.wolfmq.domain.server.store.CheckPoint;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.StoreState;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
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
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterMetaBeans(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        context.register(storeConfig);
        context.register(new StoreState(), StoreState.class);
        StoreCheckpoint __cp = new StoreCheckpoint(anotherDir.toString()); context.register(__cp, CheckPoint.class);

        ConsumeQueueProxy consumeQueueProxy = mock(ConsumeQueueProxy.class);
        StoreRegister storeRegister = mock(StoreRegister.class);
        context.register(consumeQueueProxy, ConsumeQueueProxy.class);
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
