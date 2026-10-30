package com.wolfmq.store.server.rpc;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.store.domain.mq.queue.DequeueService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.domain.mock.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RpcBootstrapTest {
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
    void initialize_ShouldCreateServerAndProcessors(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        context.register(storeConfig);
        context.register(mock(TopicStore.class), TopicStore.class);
        context.register(mock(SubscriptionStore.class), SubscriptionStore.class);
        context.register(mock(ConsumeOffsetStore.class), ConsumeOffsetStore.class);
        context.register(mock(DequeueService.class), DequeueService.class);
        context.register(mock(EnqueueService.class), EnqueueService.class);
        context.register(mock(TimerStore.class), TimerStore.class);

        RpcBootstrap bootstrap = new RpcBootstrap(context);
        assertDoesNotThrow(() -> bootstrap.initialize());
    }
}
