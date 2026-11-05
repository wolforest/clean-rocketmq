package com.wolfmq.store.domain.mq;

import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;

import com.wolfmq.common.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.server.store.api.MQStore;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.consume.ConsumeOffsetService;
import com.wolfmq.common.domain.meta.service.ConsumeOrderService;
import com.wolfmq.common.domain.mq.MQService;
import com.wolfmq.common.domain.consumer.consume.InflightCounter;
import com.wolfmq.common.core.lock.queue.DequeueLock;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.domain.offset.DefaultConsumeOffsetService;
import com.wolfmq.store.domain.meta.DefaultConsumeOrderService;
import com.wolfmq.store.domain.mq.ack.AckService;
import com.wolfmq.store.domain.mq.ack.InvisibleService;
import com.wolfmq.store.domain.mq.queue.DequeueService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.mq.offset.OffsetService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig);
        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class);

        context.register(mock(CommitLogFacade.class), CommitLogFacade.class);
        context.register(mock(ConsumeQueueFacade.class), ConsumeQueueFacade.class);
        context.register(mock(ConsumeOffsetService.class), ConsumeOffsetService.class);
        context.register(mock(ConsumeOrderService.class), ConsumeOrderService.class);
        context.register(mock(DefaultConsumeOffsetService.class), DefaultConsumeOffsetService.class);
        context.register(mock(DefaultConsumeOrderService.class), DefaultConsumeOrderService.class);
        context.register(mock(CommitLog.class), CommitLog.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterAllBeans() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap(context);
        bootstrap.initialize();

        assertNotNull(context.getBean(EnqueueService.class));
        assertNotNull(context.getBean(MessageService.class));
        assertNotNull(context.getBean(AckService.class));
        assertNotNull(context.getBean(InvisibleService.class));
        assertNotNull(context.getBean(OffsetService.class));
        assertNotNull(context.getBean(DequeueService.class));
        assertNotNull(context.getBean(MQService.class));
        assertNotNull(context.getBean(DequeueLock.class));
        assertNotNull(context.getBean(InflightCounter.class));
    }

    @Test
    void initialize_ShouldRegisterMQStoreAPI() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap(context);
        bootstrap.initialize();

        assertNotNull(context.getBean(MQStore.class, ContextNamespace.API));
    }
}
