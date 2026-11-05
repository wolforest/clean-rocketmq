package com.wolfmq.store.domain.mq.ack;

import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.consumer.consume.InflightCounter;
import com.wolfmq.common.core.lock.queue.DequeueLock;
import com.wolfmq.common.testing.ConfigMock;
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

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig);
        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class);

        context.register(new DequeueLock());
        context.register(new InflightCounter());
        context.register(mock(DefaultConsumeOffsetService.class), DefaultConsumeOffsetService.class);
        context.register(mock(DefaultConsumeOrderService.class), DefaultConsumeOrderService.class);
        context.register(mock(EnqueueService.class), EnqueueService.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterAckService() throws Exception {
        AckBootstrap bootstrap = new AckBootstrap(context);
        bootstrap.initialize();

        AckService ackService = context.getBean(AckService.class);
        assertNotNull(ackService);
    }

    @Test
    void initialize_ShouldRegisterInvisibleService() throws Exception {
        AckBootstrap bootstrap = new AckBootstrap(context);
        bootstrap.initialize();

        InvisibleService invisibleService = context.getBean(InvisibleService.class);
        assertNotNull(invisibleService);
    }
}
