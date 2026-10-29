package com.wolfmq.store.domain.mq.ack;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.consumer.consume.InflightCounter;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.meta.DefaultConsumeOffsetService;
import com.wolfmq.store.domain.meta.DefaultConsumeOrderService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AckBootstrapTest {

    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        StoreContext.register(storeConfig);
        StoreContext.CHECK_POINT = new StoreCheckpoint(tempDir.toString());

        StoreContext.register(new DequeueLock());
        StoreContext.register(new InflightCounter());
        StoreContext.register(mock(DefaultConsumeOffsetService.class), DefaultConsumeOffsetService.class);
        StoreContext.register(mock(DefaultConsumeOrderService.class), DefaultConsumeOrderService.class);
        StoreContext.register(mock(EnqueueService.class), EnqueueService.class);
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterAckService() throws Exception {
        AckBootstrap bootstrap = new AckBootstrap();
        bootstrap.initialize();

        AckService ackService = StoreContext.getBean(AckService.class);
        assertNotNull(ackService);
    }

    @Test
    void initialize_ShouldRegisterInvisibleService() throws Exception {
        AckBootstrap bootstrap = new AckBootstrap();
        bootstrap.initialize();

        InvisibleService invisibleService = StoreContext.getBean(InvisibleService.class);
        assertNotNull(invisibleService);
    }
}
