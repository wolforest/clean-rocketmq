package com.wolfmq.store.domain.mq;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.mq.MQService;
import com.wolfmq.domain.domain.consumer.consume.InflightCounter;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.mq.ack.AckService;
import com.wolfmq.store.domain.mq.ack.InvisibleService;
import com.wolfmq.store.domain.mq.queue.DequeueService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.mq.queue.OffsetService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQBootstrapTest {

    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        StoreContext.register(storeConfig);
        StoreContext.CHECK_POINT = new StoreCheckpoint(tempDir.toString());

        StoreContext.register(mock(CommitLogManager.class), CommitLogManager.class);
        StoreContext.register(mock(ConsumeQueueManager.class), ConsumeQueueManager.class);
        StoreContext.register(mock(ConsumeOffsetService.class), ConsumeOffsetService.class);
        StoreContext.register(mock(ConsumeOrderService.class), ConsumeOrderService.class);
        StoreContext.register(mock(CommitLog.class), CommitLog.class);
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterAllBeans() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap();
        bootstrap.initialize();

        assertNotNull(StoreContext.getBean(EnqueueService.class));
        assertNotNull(StoreContext.getBean(MessageService.class));
        assertNotNull(StoreContext.getBean(AckService.class));
        assertNotNull(StoreContext.getBean(InvisibleService.class));
        assertNotNull(StoreContext.getBean(OffsetService.class));
        assertNotNull(StoreContext.getBean(DequeueService.class));
        assertNotNull(StoreContext.getBean(MQService.class));
        assertNotNull(StoreContext.getBean(DequeueLock.class));
        assertNotNull(StoreContext.getBean(InflightCounter.class));
    }

    @Test
    void initialize_ShouldRegisterMQStoreAPI() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap();
        bootstrap.initialize();

        assertNotNull(StoreContext.getAPI(MQStore.class));
    }
}
