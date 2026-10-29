package com.wolfmq.store.domain.mq.ack;

import com.wolfmq.domain.server.store.CheckPoint;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.consumer.consume.InflightCounter;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.meta.DefaultConsumeOffsetService;
import com.wolfmq.store.domain.meta.DefaultConsumeOrderService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AckBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig, ContextNamespace.APP);
        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class, ContextNamespace.APP); context.register(__cp, StoreCheckpoint.class, ContextNamespace.APP);

        context.register(new DequeueLock(), ContextNamespace.APP);
        context.register(new InflightCounter(), ContextNamespace.APP);
        context.register(mock(DefaultConsumeOffsetService.class), DefaultConsumeOffsetService.class, ContextNamespace.APP);
        context.register(mock(DefaultConsumeOrderService.class), DefaultConsumeOrderService.class, ContextNamespace.APP);
        context.register(mock(EnqueueService.class), EnqueueService.class, ContextNamespace.APP);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterAckService() throws Exception {
        AckBootstrap bootstrap = new AckBootstrap(context);
        bootstrap.initialize();

        AckService ackService = context.getBean(AckService.class, ContextNamespace.APP);
        assertNotNull(ackService);
    }

    @Test
    void initialize_ShouldRegisterInvisibleService() throws Exception {
        AckBootstrap bootstrap = new AckBootstrap(context);
        bootstrap.initialize();

        InvisibleService invisibleService = context.getBean(InvisibleService.class, ContextNamespace.APP);
        assertNotNull(invisibleService);
    }
}
